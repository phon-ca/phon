package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.session.editor.view.transcriptEditor.TranscriptDocument;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptStyleConstants;
import ca.phon.app.session.editor.view.transcriptEditor.hooks.TranscriptDocumentInsertedTierHook;
import ca.phon.app.session.editor.view.transcriptEditor.hooks.TranscriptDocumentInsertionHook;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.util.ArrayList;
import java.util.List;

public class SyllabificationExtensions implements TranscriptEditorExtension {

    private TranscriptEditor editor;
    private TranscriptDocument doc;

    public SyllabificationExtensions() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        this.doc = editor.getTranscriptDocument();

        doc.addInsertedTierHook(new TranscriptDocumentInsertedTierHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> insertedTier(MutableAttributeSet attrs) {
                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                if (!editor.isSyllabificationVisible()) return retVal;

                Tier<?> tier = (Tier<?>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (tier != null && tier.getDeclaredType().equals(IPATranscript.class)) {
                    if (tier.getName().equals("IPA Target") || tier.getName().equals("IPA Actual")) {
                        Tier<IPATranscript> ipaTier = (Tier<IPATranscript>) tier;

                        // Add a newline at the end of the regular tier content
                        retVal.addAll(doc.getBatchEndLineFeed(attrs));
                        // Create a dummy tier for the syllabification
                        IPATranscript ipaTarget = ipaTier.getValue();
                        Tier<IPATranscript> syllableTier = doc.getSessionFactory().createTier(
                            tier.getName().equals("IPA Target") ? SystemTierType.TargetSyllables.getName() : SystemTierType.ActualSyllables.getName(),
                            IPATranscript.class
                        );
                        syllableTier.setValue(ipaTarget);
                        // Set up the tier attributes for the dummy tier
                        attrs = new SimpleAttributeSet(attrs);
                        attrs.addAttributes(doc.getTierAttributes(syllableTier));
                        // Set up the attributes for its label
                        SimpleAttributeSet syllabificationLabelAttrs = doc.getTierLabelAttributes(syllableTier);
                        Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
                        if (record != null) {
                            syllabificationLabelAttrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD, record);
                        }
                        // Get the string for the label
                        String syllabificationLabelText = doc.formatLabelText("Syllabification");
                        // Add the label
                        appendBatchString(syllabificationLabelText + ": ", syllabificationLabelAttrs);
                        // Add component factory if needed
                        if (doc.isSyllabificationComponent()) {
                            attrs.addAttributes(doc.getSyllabificationAttributes());
                        }
                        // Append the content
                        doc.formatSyllabification(syllableTier.getValue(), attrs);
                    }
                }

                return retVal;
            }
        });
    }
}
