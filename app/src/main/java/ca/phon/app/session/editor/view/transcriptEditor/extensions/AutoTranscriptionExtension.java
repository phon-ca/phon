package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.session.editor.autotranscribe.AutoTranscriber;
import ca.phon.app.session.editor.view.ipa_lookup.IPALookupEdit;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptLocation;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.Transcript;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

/**
 * An extension that provides auto-transcription support to the {@link TranscriptEditor}
 * */
public class AutoTranscriptionExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    private Supplier<IPADictionary> ipaDictionarySupplier;

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
    }

    /**
     * Automatically transcribes an IPA tier if the caret is currently in one
     * */
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
