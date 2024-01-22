package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.session.Record;
import ca.phon.session.Tier;

import javax.swing.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension that provides blind transcription support to the {@link TranscriptEditor}
 * */
public class BlindTranscriptionExtension implements TranscriptEditorExtension {
    private TranscriptDocument doc = null;

    /* Document property stuff */

    public final static String VALIDATION_MODE = "isValidationMode";
    public final static boolean VALIDATION_MODE_DEFAULT = false;

    @Override
    public void install(TranscriptEditor editor) {
        this.doc = editor.getTranscriptDocument();

        doc.addDocumentPropertyChangeListener(VALIDATION_MODE, evt -> doc.reload());

        doc.addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {

                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);

                if (isValidationMode() && tier.isBlind()) {
                    List<String> transcribers = tier.getTranscribers();
                    for (String transcriber : transcribers) {
                        retVal.addAll(TranscriptBatchBuilder.getBatchEndLineFeed(attrs, null));
                        retVal.addAll(getBlindTranscription(tier, transcriber, record));
                        attrs = new SimpleAttributeSet(retVal.get(retVal.size() - 1).getAttributes());
                        attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_COMPONENT_FACTORY);
                    }
                }

                return retVal;
            }
        });
    }


    private boolean isValidationMode() {
        return (boolean) doc.getDocumentPropertyOrDefault(
            VALIDATION_MODE,
            VALIDATION_MODE_DEFAULT
        );
    }

    /**
     * Gets a list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} that contains the data for a blind
     * transcription line for a specified tier from a specified transcriber
     *
     * @param tier a reference to the tier that the blind transcription belongs to
     * @param transcriber the name / id of the transcriber whose transcription will go on the line
     * @param record a reference to the record that the tier belongs to
     *
     * @return the list of {@link javax.swing.text.DefaultStyledDocument.ElementSpec} data
     * */
    private List<DefaultStyledDocument.ElementSpec> getBlindTranscription(Tier<?> tier, String transcriber, Record record) {

        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

        TranscriptStyleContext transcriptStyleContext = doc.getTranscriptStyleContext();

        SimpleAttributeSet blindTranscriptionAttrs = transcriptStyleContext.getBlindTranscriptionAttributes(tier, transcriber);
        StyleConstants.setForeground(blindTranscriptionAttrs, UIManager.getColor(TranscriptEditorUIProps.BLIND_TRANSCRIPTION_FOREGROUND));
        SimpleAttributeSet labelAttrs = new SimpleAttributeSet(blindTranscriptionAttrs);
        labelAttrs.addAttributes(transcriptStyleContext.getLabelAttributes());

        String labelText = "\t" + transcriber;

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        retVal.add(TranscriptBatchBuilder.getBatchString(labelText, labelAttrs));

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        retVal.add(TranscriptBatchBuilder.getBatchString(": ", labelAttrs));

        String transcriptionText = doc.getTierText(tier, transcriber);
        retVal.add(TranscriptBatchBuilder.getBatchString(transcriptionText, blindTranscriptionAttrs));

        retVal.add(TranscriptBatchBuilder.getBatchString(" ", transcriptStyleContext.getTranscriptionSelectorAttributes(record, tier, transcriptionText, doc.getSession(), doc.getEventManager(), doc.getUndoSupport())));

        return retVal;
    }
}
