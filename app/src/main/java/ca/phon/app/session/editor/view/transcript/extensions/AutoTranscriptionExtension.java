package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.function.Supplier;

/**
 * An extension that provides auto-transcription support to the {@link TranscriptEditor}
 * */
public class AutoTranscriptionExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    private Supplier<IPADictionary> ipaDictionarySupplier;

    private Range ghostRange = null;

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

        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged, (e) -> {
            if(ghostRange != null) {
                try {
                    editor.getTranscriptDocument().remove(ghostRange.getFirst(), ghostRange.getLast() - ghostRange.getFirst());
                    ghostRange = null;
                } catch (BadLocationException ex) {
                    LogUtil.warning(ex);
                }
            }

            final AttributeSet eleAttrs = editor.getTranscriptDocument().getCharacterElement(editor.getCaretPosition()).getAttributes();
            final Tier<?> tier = (Tier<?>)eleAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
            final Record record = (Record)eleAttrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
            if(record != null && tier != null) {
                if(tier.getDeclaredType().equals(IPATranscript.class)) {
                    final IPATranscript tierVal = tier.hasValue() ? (IPATranscript) tier.getValue() : new IPATranscript();
                    if(!tier.isUnvalidated() && tierVal.length() == 0) {
                        IPATranscript suggestion = new IPATranscript();
                        if(SystemTierType.IPAActual.getName().equals(tier.getName())) {
                            if(record.getIPATargetTier().hasValue() && record.getIPATargetTier().getValue().length() > 0) {
                                try {
                                    suggestion = IPATranscript.parseIPATranscript(record.getIPATarget().toString());
                                } catch (ParseException ex) {
                                    LogUtil.warning(ex);
                                }
                            }
                        }
                        if(suggestion.length() == 0)
                            suggestion = AutoTranscriber.transcribe(record.getOrthography(), getDictionary());
                        final IPATranscript autoTranscript = suggestion;
                        if(autoTranscript.length() > 0) {
                            final SimpleAttributeSet ghostAttrs = editor.getTranscriptDocument().getTranscriptStyleContext().getTierAttributes(tier);
                            // make text gray and italic
                            StyleConstants.setForeground(ghostAttrs, Color.gray);
                            StyleConstants.setItalic(ghostAttrs, true);
                            ghostAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
                            ghostAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ELEMENT_TYPE, "record");

                            final PhonUIAction<Void> acceptAutoTranscriptionAct = PhonUIAction.runnable(() -> {
                                if(ghostRange != null) {
                                    try {
                                        editor.getTranscriptDocument().remove(ghostRange.getStart(), ghostRange.getEnd() - ghostRange.getStart());
                                        ghostRange = null;
                                    } catch (BadLocationException ex) {
                                        LogUtil.warning(ex);
                                    }
                                }
                                final TierEdit<IPATranscript> edit = new TierEdit<>(editor.getSession(), editor.getEventManager(), record, (Tier<IPATranscript>)tier, autoTranscript);
                                edit.setValueAdjusting(false);
                                editor.getUndoSupport().postEdit(edit);
                            });
                            ghostAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ENTER_ACTION, acceptAutoTranscriptionAct);

                            try {
                                TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder();
                                batchBuilder.appendBatchString(autoTranscript.toString(), ghostAttrs);
                                editor.getTranscriptDocument().processBatchUpdates(editor.getCaretPosition(), batchBuilder.getBatch());
                                ghostRange = new Range(editor.getCaretPosition(), editor.getCaretPosition() + autoTranscript.toString().length());
                            } catch (BadLocationException ex) {
                                LogUtil.warning(ex);
                            }
                        }
                    }
                }
            }
        });
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
}
