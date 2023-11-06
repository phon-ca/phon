package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.session.editor.view.transcriptEditor.*;
import ca.phon.session.Record;
import ca.phon.session.Tier;

import javax.swing.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

public class BlindTranscriptionExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor = null;
    private TranscriptDocument doc = null;
    public final static String VALIDATION_MODE = "isValidationMode";
    public final static boolean VALIDATION_MODE_DEFAULT = false;
    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        this.doc = editor.getTranscriptDocument();

        doc.addDocumentPropertyChangeListener(VALIDATION_MODE, evt -> {
            doc.reload();
        });

        doc.addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {

                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);

                if (isValidationMode() && tier.isBlind()) {
                    List<String> transcribers = tier.getTranscribers();
                    for (String transcriber : transcribers) {
                        retVal.addAll(doc.getBatchEndLineFeed(attrs));
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

    private List<DefaultStyledDocument.ElementSpec> getBlindTranscription(Tier<?> tier, String transcriber, Record record) {

        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

        SimpleAttributeSet blindTranscriptionAttrs = doc.getBlindTranscriptionAttrs(tier, transcriber);
        blindTranscriptionAttrs.addAttributes(doc.getStandardFontAttributes());
        StyleConstants.setForeground(blindTranscriptionAttrs, UIManager.getColor(TranscriptEditorUIProps.BLIND_TRANSCRIPTION_FOREGROUND));
        SimpleAttributeSet labelAttrs = new SimpleAttributeSet(blindTranscriptionAttrs);
        labelAttrs.addAttributes(doc.getLabelAttributes());

        String labelText = transcriber;
        if (labelText.length() < doc.getLabelColumnWidth()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < (doc.getLabelColumnWidth() - labelText.length()); i++) {
                builder.append(' ');
            }
            retVal.add(doc.getBatchString(builder.toString(), labelAttrs));
        }
        else {
            labelText = doc.formatLabelText(labelText);
        }

        labelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE, true);
        retVal.add(doc.getBatchString(labelText, labelAttrs));

        labelAttrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_CLICKABLE);
        retVal.add(doc.getBatchString(": ", labelAttrs));

        String transcriptionText = doc.getTierText(tier, transcriber);
        retVal.add(doc.getBatchString(transcriptionText, blindTranscriptionAttrs));

        retVal.add(doc.getBatchString(" ", doc.getTranscriptionSelectorAttributes(record, tier, transcriptionText)));

        return retVal;
    }
}
