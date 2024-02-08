package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.autotranscribe.AutoTranscriber;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.ipaDictionary.IPALookupEdit;
import ca.phon.app.session.editor.view.transcript.TranscriptBatchBuilder;
import ca.phon.app.session.editor.view.transcript.TranscriptLocation;
import ca.phon.app.session.editor.view.transcript.TranscriptEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptStyleConstants;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.Transcript;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Range;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.function.Supplier;

/**
 * An extension that provides auto-transcription support to the {@link TranscriptEditor}
 * */
public class AutoTranscriptionExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    private Supplier<IPADictionary> ipaDictionarySupplier;

    private Range ghostRange = null;

    private final static String AUTO_TRANSCRIPTION_ATTR = "autoTranscription";

    public AutoTranscriptionExtension() {
        this(() -> IPADictionaryLibrary.getInstance().defaultDictionary());
    }

    public AutoTranscriptionExtension(Supplier<IPADictionary> ipaDictionarySupplier) {
        this.ipaDictionarySupplier = ipaDictionarySupplier;
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

    /**
     * Insert ghost text for auto-transcription
     *
     * @param ipa
     */
    private void insertGhostText(Record record, Tier<IPATranscript> tier, IPATranscript ipa) {
        final SimpleAttributeSet ghostAttrs = editor.getTranscriptDocument().getTranscriptStyleContext().getTierAttributes(tier);
        // make text gray and italic
        StyleConstants.setForeground(ghostAttrs, Color.gray);
        StyleConstants.setItalic(ghostAttrs, true);
        TranscriptStyleConstants.setRecord(ghostAttrs, record);
        TranscriptStyleConstants.setNotTraversable(ghostAttrs, true);
        TranscriptStyleConstants.setElementType(ghostAttrs, TranscriptStyleConstants.ELEMENT_TYPE_RECORD);
        ghostAttrs.addAttribute(AUTO_TRANSCRIPTION_ATTR, ipa);
        try {
            editor.getTranscriptEditorCaret().freeze();
            TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
            batchBuilder.appendBatchString(ipa.toString(), ghostAttrs);
            editor.getTranscriptDocument().processBatchUpdates(editor.getCaretPosition(), batchBuilder.getBatch());
            ghostRange = new Range(editor.getCaretPosition(), editor.getCaretPosition() + ipa.toString().length());
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
                editor.getTranscriptDocument().remove(ghostRange.getStart(), ghostRange.getEnd() - ghostRange.getStart());
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
     * @param ipa
     *
     */
    public void acceptAutoTranscription(Record record, Tier<IPATranscript> tier, IPATranscript ipa) {


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
                if(!tier.isUnvalidated() && tierVal.length() == 0 && e.getData().get().newLoc().posInTier() == 0) {
                    final IPATranscript autoTranscript = AutoTranscriber.transcribe(record.getOrthography(), getDictionary());
                    if(autoTranscript.length() > 0) {
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
     * Automatically transcribes an IPA tier if the caret is currently in one
     **/
    public void autoTranscription() {
        TranscriptLocation transcriptLocation = editor.getCurrentSessionLocation();
        Transcript.Element elem = editor.getSession().getTranscript().getElementAt(transcriptLocation.elementIndex());
        if (!elem.isRecord()) return;
        Record record = elem.asRecord();
        String tierName = transcriptLocation.label();
        if (elem.isRecord() && record.hasTier(tierName)) {
            Tier<?> tier = record.getTier(tierName);
            if (!tier.getDeclaredType().equals(IPATranscript.class)) return;

            AutoTranscriber autoTranscriber = new AutoTranscriber(editor.getSession(), editor.getEventManager());
            autoTranscriber.setDictionary(getDictionary());
            IPALookupEdit edit = autoTranscriber.transcribeTier(record, (Tier<IPATranscript>) tier);
            editor.getUndoSupport().postEdit(edit);
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
                            final IPATranscript ipa = (IPATranscript)eleAttrs.getAttribute(AUTO_TRANSCRIPTION_ATTR);
                            removeGhostRange();
                            acceptAutoTranscription(record, tier, ipa);
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
                if(ghostRange != null && (pos < ghostRange.getStart() || pos > ghostRange.getEnd())) {
                    removeGhostRange();
                    Toolkit.getDefaultToolkit().removeAWTEventListener(alignmentListener);
                } else {
                    me.consume();
                }
            }
        }
    };
}
