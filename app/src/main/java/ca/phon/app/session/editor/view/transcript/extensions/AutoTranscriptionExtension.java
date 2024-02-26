package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.transcript.TranscriptBatchBuilder;
import ca.phon.app.session.editor.view.transcript.TranscriptDocument;
import ca.phon.app.session.editor.view.transcript.TranscriptEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptStyleConstants;
import ca.phon.autotranscribe.AutoTranscriber;
import ca.phon.autotranscribe.AutomaticTranscription;
import ca.phon.app.session.editor.autotranscribe.IPADictionaryAutoTranscribeSource;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.orthography.Orthography;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.Transcript;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Range;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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

    public AutoTranscriptionExtension() {
        this(() -> IPADictionaryLibrary.getInstance().defaultDictionary());
    }

    public AutoTranscriptionExtension(Supplier<IPADictionary> ipaDictionarySupplier) {
        this.ipaDictionarySupplier = ipaDictionarySupplier;

        autoTranscriber = new AutoTranscriber();
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

        InputMap inputMap = editor.getInputMap();
        ActionMap actionMap = editor.getActionMap();

        KeyStroke autoTranscription = KeyStroke.getKeyStroke(
            KeyEvent.VK_T,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK
        );
        inputMap.put(autoTranscription, "autoTranscription");
        PhonUIAction<Void> autoTranscriptionAct = PhonUIAction.runnable(this::autoTranscription);
        actionMap.put("autoTranscription", autoTranscriptionAct);

        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged, this::onTranscriptLocationChanged,
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
        final IPATranscript ipa = automaticTranscription.getTranscription();
        final TierEdit<IPATranscript> edit = new TierEdit<>(editor.getSession(), editor.getEventManager(), editor.getDataModel().getTranscriber(), record, tier, ipa);
        edit.setValueAdjusting(false);
        editor.getUndoSupport().postEdit(edit);
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
                final IPATranscript tierVal = tier.hasValue() ? (IPATranscript) tier.getValue() : new IPATranscript();
                if(!tier.isUnvalidated() && tierVal.length() == 0 && e.getData().get().newLoc().charPosition() == 0) {
                    final Orthography orthography = getOrthography(record, editor.getDataModel().getTranscriber());
                    final AutomaticTranscription autoTranscript = autoTranscriber.transcribe(orthography);
                    if(autoTranscript.getWords().size() > 0) {
                        final SimpleAttributeSet ghostAttrs = editor.getTranscriptDocument().getTranscriptStyleContext().getTierAttributes(tier);
                        // make text gray and italic
                        StyleConstants.setForeground(ghostAttrs, Color.gray);
                        StyleConstants.setItalic(ghostAttrs, true);
                        TranscriptStyleConstants.setNotTraversable(ghostAttrs, true);
                        TranscriptStyleConstants.setElementType(ghostAttrs, TranscriptStyleConstants.ELEMENT_TYPE_RECORD);

                        insertGhostText(record, (Tier<IPATranscript>)tier, autoTranscript);
                        Toolkit.getDefaultToolkit().addAWTEventListener(alignmentListener, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
                    }
                }
            }
        }
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
    public void autoTranscription() {
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
                        case KeyEvent.VK_TAB, KeyEvent.VK_ENTER -> {
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
