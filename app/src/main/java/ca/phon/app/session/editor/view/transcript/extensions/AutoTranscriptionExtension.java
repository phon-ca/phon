package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.alignedTypesDatabase.AlignedTypesDatabase;
import ca.phon.alignedTypesDatabase.AlignedTypesDatabaseFactory;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.autotranscribe.AlignedTypesAutoTranscribeSource;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.transcript.TranscriptBatchBuilder;
import ca.phon.app.session.editor.view.transcript.TranscriptDocument;
import ca.phon.app.session.editor.view.transcript.TranscriptEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptStyleConstants;
import ca.phon.autotranscribe.AutoTranscriber;
import ca.phon.autotranscribe.AutomaticTranscription;
import ca.phon.app.session.editor.autotranscribe.IPADictionaryAutoTranscribeSource;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.Word;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.alignment.CrossTierAlignment;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Supplier;

/**
 * An extension that provides auto-transcription support to the {@link TranscriptEditor}
 */
public class AutoTranscriptionExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    private Supplier<IPADictionary> ipaDictionarySupplier;

    private TranscriptDocument.StartEnd ghostRange = null;

    private final static String AUTO_TRANSCRIPTION_ATTR = "autoTranscription";

    private final AutoTranscriber autoTranscriber;

    /*
     * Aligned types database including data from all IPA tiers in session.  These are used to
     * provide automatic transcriptions for IPA tiers suggesting previously used values.
     */
    private AlignedTypesDatabase alignedTypesDatabase;

    public AutoTranscriptionExtension() {
        this(() -> IPADictionaryLibrary.getInstance().defaultDictionary());
    }

    public AutoTranscriptionExtension(Supplier<IPADictionary> ipaDictionarySupplier) {
        this.ipaDictionarySupplier = ipaDictionarySupplier;

        autoTranscriber = new AutoTranscriber();
        this.alignedTypesDatabase = AlignedTypesDatabaseFactory.newDatabase();
        autoTranscriber.addSource(new AlignedTypesAutoTranscribeSource(alignedTypesDatabase));
        autoTranscriber.addSource(new IPADictionaryAutoTranscribeSource(getDictionary()));
    }

    public IPADictionary getDictionary() {
        var retVal = ipaDictionarySupplier.get();
        if(retVal == null)
            retVal = IPADictionaryLibrary.getInstance().defaultDictionary();
        return retVal;
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;

        PhonWorker.invokeOnNewWorker(this::scanSession);

        InputMap inputMap = editor.getInputMap();
        ActionMap actionMap = editor.getActionMap();

        KeyStroke autoTranscription = KeyStroke.getKeyStroke(
            KeyEvent.VK_T,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
        );
        inputMap.put(autoTranscription, "autoTranscription");
        PhonUIAction<Void> autoTranscriptionAct = PhonUIAction.runnable(this::onAutoTranscribe);
        actionMap.put("autoTranscription", autoTranscriptionAct);

        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged, this::onTranscriptLocationChanged,
                EditorEventManager.RunOn.AWTEventDispatchThread);
        editor.getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onTierChange,
                EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private void addAutomaticTranscription(MutableAttributeSet attrs, AutomaticTranscription autoTranscription) {
        attrs.addAttribute(AUTO_TRANSCRIPTION_ATTR, autoTranscription);
    }

    private void removeAutomaticTranscription(MutableAttributeSet attrs) {
        attrs.removeAttribute(AUTO_TRANSCRIPTION_ATTR);
    }

    private AutomaticTranscription getAutomaticTranscription(AttributeSet attrs) {
        return (AutomaticTranscription)attrs.getAttribute(AUTO_TRANSCRIPTION_ATTR);
    }

    /**
     * Insert ghost text for auto-transcription
     *
     * @param automaticTranscription
     */
    private void insertGhostText(Record record, Tier<IPATranscript> tier, AutomaticTranscription automaticTranscription) {
        if(PrefHelper.getBoolean("phon.debug", false)) {
            LogUtil.info("Inserting ghost text for auto-transcription");
        }
        final SimpleAttributeSet ghostAttrs = editor.getTranscriptDocument().getTranscriptStyleContext().getTierAttributes(tier);
        // make text gray and italic
        StyleConstants.setForeground(ghostAttrs, Color.gray);
        StyleConstants.setItalic(ghostAttrs, true);
        TranscriptStyleConstants.setRecord(ghostAttrs, record);
        TranscriptStyleConstants.setNotTraversable(ghostAttrs, true);
        TranscriptStyleConstants.setElementType(ghostAttrs, TranscriptStyleConstants.ELEMENT_TYPE_RECORD);
        addAutomaticTranscription(ghostAttrs, automaticTranscription);
        try {
            editor.getTranscriptEditorCaret().freeze();
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
            final IPATranscript ipa = automaticTranscription.getTranscription();
            batchBuilder.appendBatchString(ipa.toString(), ghostAttrs);
            editor.getTranscriptDocument().processBatchUpdates(editor.getCaretPosition(), batchBuilder.getBatch());
            ghostRange = new TranscriptDocument.StartEnd(editor.getCaretPosition(), editor.getCaretPosition() + ipa.toString().length());
            System.out.println("Ghost range: " + ghostRange);
        } catch (BadLocationException ex) {
            LogUtil.warning(ex);
        }
    }

    /**
     * Remove ghost text from document and unfreeze caret
     *
     */
    public void removeGhostRange() {
        if(ghostRange != null) {
            if(PrefHelper.getBoolean("phon.debug", false)) {
                LogUtil.info("Removing ghost text");
            }
            try {
                editor.getTranscriptDocument().remove(ghostRange.start(), ghostRange.length());
                ghostRange = null;
                editor.getTranscriptEditorCaret().unfreeze();
            } catch (BadLocationException ex) {
                LogUtil.warning(ex);
            }
        }
    }

    /**
     * Accept automatic transcription
     *
     * @param record
     * @param tier
     * @param automaticTranscription
     *
     */
    public void acceptAutoTranscription(Record record, Tier<IPATranscript> tier, AutomaticTranscription automaticTranscription) {
        final IPATranscriptBuilder builder = new IPATranscriptBuilder();

        final TranscriptDocument.StartEnd currentTextRange = editor.getTranscriptDocument().getTierContentRange(tier);
        try {
            final String currentText = editor.getTranscriptDocument().getText(currentTextRange.start(), currentTextRange.length());
            if(!currentText.isBlank()) {
                builder.append(currentText.trim());
            }
        } catch(BadLocationException ex) {
            LogUtil.warning(ex);
        }

        final IPATranscript ipa = automaticTranscription.getTranscription();
        if(ipa.length() > 0) {
            if(builder.size() > 0)
                builder.appendWordBoundary();
            builder.append(ipa);
        }
        final TierEdit<IPATranscript> edit = new TierEdit<>(editor.getSession(), editor.getEventManager(), editor.getDataModel().getTranscriber(), record, tier, builder.toIPATranscript());
        edit.setValueAdjusting(false);
        editor.getUndoSupport().postEdit(edit);
    }

    /**
     * Accept automatic transcription up to first word with options
     *
     * @param record
     * @param tier
     * @param automaticTranscription
     *
     */
    public void acceptAutoTranscriptionToFirstSelection(Record record, Tier<IPATranscript> tier, AutomaticTranscription automaticTranscription) {
        final IPATranscriptBuilder builder = new IPATranscriptBuilder();

        final TranscriptDocument.StartEnd currentTextRange = editor.getTranscriptDocument().getTierContentRange(tier);
        try {
            final String currentText = editor.getTranscriptDocument().getText(currentTextRange.start(), currentTextRange.length());
            if(!currentText.isBlank()) {
                builder.append(currentText.trim());
            }
        } catch(BadLocationException ex) {
            LogUtil.warning(ex);
        }

        boolean incomplete = false;
        for(int i = 0; i < automaticTranscription.getWords().size(); i++) {
            final Word word = automaticTranscription.getWords().get(i);
            if(i > 0 && automaticTranscription.getTranscriptionOptions(word).length > 1) {
                incomplete = true;
                break;
            }
            if(builder.size() > 0)
                builder.appendWordBoundary();
            builder.append(automaticTranscription.getSelectedTranscription(word));
        }

        if(incomplete)
            builder.appendWordBoundary();

        final IPATranscript ipa = builder.toIPATranscript();
        try {
            final int start = currentTextRange.start();
            editor.getTranscriptEditorCaret().freeze();
            editor.getTranscriptDocument().remove(start, currentTextRange.length());

            final SimpleAttributeSet attrs = new SimpleAttributeSet();
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
            TranscriptStyleConstants.setTier(attrs, tier);
            TranscriptStyleConstants.setRecord(attrs, record);
            TranscriptStyleConstants.setElementType(attrs, TranscriptStyleConstants.ELEMENT_TYPE_RECORD);
            batchBuilder.appendBatchString(ipa.toString(), attrs);

            editor.getTranscriptDocument().processBatchUpdates(start, batchBuilder.getBatch());
            editor.getTranscriptEditorCaret().unfreeze();
            editor.getTranscriptEditorCaret().setDot(start + ipa.toString().length());
        } catch(BadLocationException ex) {
            LogUtil.warning(ex);
        }
    }

    /**
     * On transcript location changed.  Check for an empty IPA tier and suggest transcription
     *
     * @param e
     */
    private void onTranscriptLocationChanged(EditorEvent<TranscriptEditor.TranscriptLocationChangeData> e) {
        final AttributeSet eleAttrs = editor.getTranscriptDocument().getCharacterElement(editor.getCaretPosition()).getAttributes();
        final Tier<?> tier = TranscriptStyleConstants.getTier(eleAttrs);
        final Record record = TranscriptStyleConstants.getRecord(eleAttrs);
        if(record != null && tier != null) {
            if(tier.getDeclaredType().equals(IPATranscript.class)) {
                final TranscriptDocument.StartEnd currentTextRange = editor.getTranscriptDocument().getTierContentRange(tier);
                try {
                    final String currentText = editor.getTranscriptDocument().getText(currentTextRange.start(), currentTextRange.length());
                    final int numWords = currentText.isBlank() ? 0 : currentText.trim().split("\\p{Space}").length;
                    System.out.println("Current text: " + currentText + ", numWords: " + numWords);
                    if(!tier.isUnvalidated()) {
                        if(currentText.length() == 0 && e.getData().get().newLoc().charPosition() == 0) {
                            final Orthography orthography = getOrthography(record, editor.getDataModel().getTranscriber());
                            final AutomaticTranscription autoTranscript = autoTranscriber.transcribe(orthography);
                            if(autoTranscript.getWords().size() > 0) {
                                insertAutomaticTranscription(record, (Tier<IPATranscript>)tier, autoTranscript);
                            }
                        } else if(e.getData().get().newLoc().charPosition() == currentText.length()) {
                            // get previous character, if space then suggest transcription from current word index
                            final int prevCharPos = editor.getTranscriptEditorCaret().getDot() - 1;
                            final char prevChar = editor.getTranscriptDocument().getText(prevCharPos, 1).charAt(0);
                            if (Character.isWhitespace(prevChar)) {
                                final Orthography orthography = getOrthography(record, editor.getDataModel().getTranscriber());
                                final AutomaticTranscription autoTranscript = autoTranscriber.transcribe(orthography, numWords);
                                if(autoTranscript.getWords().size() > 0) {
                                    insertAutomaticTranscription(record, (Tier<IPATranscript>)tier, autoTranscript);
                                }
                            }
                        }
                    }
                } catch(BadLocationException ex) {
                    LogUtil.warning(ex);
                }
            }
        }
    }

    private void insertAutomaticTranscription(Record record, Tier<IPATranscript> tier, AutomaticTranscription autoTranscript) {
        final SimpleAttributeSet ghostAttrs = editor.getTranscriptDocument().getTranscriptStyleContext().getTierAttributes(tier);
        // make text gray and italic
        StyleConstants.setForeground(ghostAttrs, Color.gray);
        StyleConstants.setItalic(ghostAttrs, true);
        TranscriptStyleConstants.setNotTraversable(ghostAttrs, true);
        TranscriptStyleConstants.setElementType(ghostAttrs, TranscriptStyleConstants.ELEMENT_TYPE_RECORD);

        insertGhostText(record, (Tier<IPATranscript>)tier, autoTranscript);
        Toolkit.getDefaultToolkit().addAWTEventListener(alignmentListener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Get orthography for given record
     *
     * @param record
     * @param transcriber
     */
    public Orthography getOrthography(Record record, Transcriber transcriber) {
        return (transcriber == Transcriber.VALIDATOR ? record.getOrthography() : record.getOrthographyTier().getBlindTranscription(transcriber.getUsername()));
    }

    /**
     * Automatically transcribes an IPA tier if the caret is currently in one
     **/
    public void onAutoTranscribe() {
        TranscriptElementLocation transcriptLocation = editor.getCurrentSessionLocation();
        Transcript.Element elem = editor.getSession().getTranscript().getElementAt(transcriptLocation.transcriptElementIndex());
        if (!elem.isRecord()) return;
        Record record = elem.asRecord();
        String tierName = transcriptLocation.tier();
        if (elem.isRecord() && record.hasTier(tierName)) {
            Tier<?> tier = record.getTier(tierName);
            if (!tier.getDeclaredType().equals(IPATranscript.class)) return;

            final AutomaticTranscription automaticTranscription = autoTranscriber.transcribe(getOrthography(record, editor.getDataModel().getTranscriber()));

            final IPATranscript newVal = automaticTranscription.getTranscription();
            if(newVal.length() > 0) {
                final TierEdit<IPATranscript> edit = new TierEdit<>(editor.getSession(), editor.getEventManager(),
                        editor.getDataModel().getTranscriber(), record, (Tier<IPATranscript>)tier, newVal, false);
                editor.getUndoSupport().postEdit(edit);
            }
        }
    }

    private void onTierChange(EditorEvent<EditorEventType.TierChangeData> evt) {
        if (evt.data().valueAdjusting()) return;
        final Tier<?> tier = evt.data().tier();
        if (tier.getDeclaredType().equals(IPATranscript.class)) {
            final Record record = evt.data().record();

            final Tier<IPATranscript> tempTier = SessionFactory.newFactory().createTier(tier.getName(), IPATranscript.class);
            tempTier.setValue((IPATranscript) evt.getData().get().oldValue());
            final TierAlignment oldAlignment = TierAligner.alignTiers(record.getOrthographyTier(), tempTier);
            removeOldAlignmentValues(oldAlignment);

            final TierAlignment alignment = TierAligner.alignTiers(record.getOrthographyTier(), tier);
            scanTierAlignment(alignment);
        } else if(tier.getDeclaredType().equals(Orthography.class)) {
            if(ghostRange != null && ghostRange.valid()) {
                final Element ele = editor.getTranscriptDocument().getCharacterElement(ghostRange.start());
                final AttributeSet attrs = ele.getAttributes();
                final Tier<IPATranscript> ipaTier = (Tier<IPATranscript>) TranscriptStyleConstants.getTier(attrs);
                removeGhostRange();
                final Record record = evt.data().record();
                final Orthography orthography = (Orthography)evt.data().newValue();
                final AutomaticTranscription autoTranscription = autoTranscriber.transcribe(orthography);
                if(autoTranscription.getWords().size() > 0) {
                    insertAutomaticTranscription(record, ipaTier, autoTranscription);
                }
            }
        }
    }

    /**
     * Remove old alignment values from aligned types database
     *
     * @param alignment
     */
    private void removeOldAlignmentValues(TierAlignment alignment) {
        for (var alignedEle : alignment.getAlignedElements()) {
            final IPATranscript ipa = (IPATranscript) alignedEle.getObj2();
            // ignore empty transcriptions
            if (ipa != null && ipa.length() > 0 && !"*".equals(ipa.toString())) {
                alignedTypesDatabase.removeAlignment(SystemTierType.Orthography.getName(), alignedEle.getObj1().toString(),
                        alignment.getBottomTier().getName(), ipa.toString());
            }
        }
    }

    /**
     * Scan session for aligned types database
     *
     */
    private void scanSession() {
        final var session = editor.getSession();
        for(Record r:session.getRecords()) {
            CrossTierAlignment alignment = TierAligner.calculateCrossTierAlignment(r, r.getOrthographyTier());
            for(Tier<IPATranscript> iapTier:r.getTiersOfType(IPATranscript.class)) {
                final TierAlignment tierAlignment = alignment.getTierAlignment(iapTier.getName());
                if(tierAlignment != null) {
                    scanTierAlignment(tierAlignment);
                }
            }
        }
    }

    private void scanTierAlignment(TierAlignment tierAlignment) {
        for(var alignedEle:tierAlignment.getAlignedElements()) {
            if(alignedEle.getObj1() == null || alignedEle.getObj2() == null) continue;
            final Word word = (Word)alignedEle.getObj1();
            final IPATranscript ipa = (IPATranscript)alignedEle.getObj2();
            // ignore empty transcriptions
            if (ipa.length() > 0 && !"*".equals(ipa.toString())) {
                alignedTypesDatabase.addAlignment(SystemTierType.Orthography.getName(),
                        word.getWord(), tierAlignment.getBottomTier().getName(), ipa.toString());
            }
        }
    }

    private final AWTEventListener alignmentListener = new AWTEventListener() {
        @Override
        public void eventDispatched(AWTEvent event) {
            if(event instanceof KeyEvent ke) {
                if(!editor.hasFocus()) return;
                if(ke.getID() == KeyEvent.KEY_PRESSED) {
                    switch (ke.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE -> {
                            removeGhostRange();
                            Toolkit.getDefaultToolkit().removeAWTEventListener(alignmentListener);
                        }
                        case KeyEvent.VK_TAB -> {
                            final AttributeSet eleAttrs = editor.getTranscriptDocument().getCharacterElement(editor.getCaretPosition()).getAttributes();
                            final Tier<IPATranscript> tier = (Tier<IPATranscript>) TranscriptStyleConstants.getTier(eleAttrs);
                            final Record record = TranscriptStyleConstants.getRecord(eleAttrs);
                            final AutomaticTranscription automaticTranscription = getAutomaticTranscription(eleAttrs);
                            removeGhostRange();
                            acceptAutoTranscriptionToFirstSelection(record, tier, automaticTranscription);
                            Toolkit.getDefaultToolkit().removeAWTEventListener(alignmentListener);
                        }
                        case KeyEvent.VK_ENTER -> {
                            final AttributeSet eleAttrs = editor.getTranscriptDocument().getCharacterElement(editor.getCaretPosition()).getAttributes();
                            final Tier<IPATranscript> tier = (Tier<IPATranscript>) TranscriptStyleConstants.getTier(eleAttrs);
                            final Record record = TranscriptStyleConstants.getRecord(eleAttrs);
                            final AutomaticTranscription automaticTranscription = getAutomaticTranscription(eleAttrs);
                            removeGhostRange();
                            acceptAutoTranscription(record, tier, automaticTranscription);
                            Toolkit.getDefaultToolkit().removeAWTEventListener(alignmentListener);
                        }
                        default -> {
                            removeGhostRange();
                            Toolkit.getDefaultToolkit().removeAWTEventListener(alignmentListener);
                            return;
                        }
                    }
                }
                ke.consume();
            } else if(event instanceof MouseEvent me) {
                // only mouse click events
                if(me.getID() != MouseEvent.MOUSE_PRESSED) return;
                if(me.getSource() != editor) return;

                final Point p = me.getPoint();

                // get character element at mouse location
                final JTextComponent textComponent = editor;
                final int pos = textComponent.viewToModel(p);
                if(pos < 0 || pos > textComponent.getDocument().getLength()) return;
                // if pos is not in ghost range remove ghost range
                if(ghostRange != null && (pos < ghostRange.start() || pos > ghostRange.end())) {
                    removeGhostRange();
                    Toolkit.getDefaultToolkit().removeAWTEventListener(alignmentListener);
                } else {
                    me.consume();
                }
            }
        }
    };
}
