package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.session.editor.view.transcriptEditor.*;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyllabificationExtension implements TranscriptEditorExtension {

    private TranscriptEditor editor;
    private TranscriptDocument doc;
    private boolean syllabificationVisible = false;
    private boolean syllabificationIsComponent = false;

    public SyllabificationExtension() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;
        this.doc = editor.getTranscriptDocument();

        doc.addInsertionHook(new TranscriptDocumentInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> endTier(MutableAttributeSet attrs) {
                List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

                if (!syllabificationVisible) return retVal;

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
                        retVal.add(doc.getBatchString(syllabificationLabelText + ": ", syllabificationLabelAttrs));
                        // Add component factory if needed
                        if (syllabificationIsComponent) {
                            attrs.addAttributes(doc.getSyllabificationAttributes());
                        }
                        // Append the content
                        retVal.addAll(getFormattedSyllabification(syllableTier.getValue(), attrs));
                    }
                }

                return retVal;
            }
        });
    }

    // region Getters and Setters

    public boolean isSyllabificationVisible() {
        return syllabificationVisible;
    }

    public void setSyllabificationVisible(boolean syllabificationVisible) {
        this.syllabificationVisible = syllabificationVisible;
        doc.putProperty("isSyllabificationVisible", syllabificationVisible);
        doc.reload();
    }

    public boolean isSyllabificationComponent() {
        return syllabificationIsComponent;
    }

    public void setSyllabificationIsComponent(boolean syllabificationIsComponent) {
        this.syllabificationIsComponent = syllabificationIsComponent;
        doc.addExtensionProperty("isSyllabificationComponent", syllabificationIsComponent);
        if (syllabificationVisible) {
            doc.reload();
        }
    }

    private List<DefaultStyledDocument.ElementSpec> getFormattedSyllabification(IPATranscript ipaTranscript, AttributeSet additionalAttrs) {
        List<DefaultStyledDocument.ElementSpec> retVal = new ArrayList<>();

        SimpleAttributeSet attrs = new SimpleAttributeSet();
        if (additionalAttrs != null) attrs.addAttributes(additionalAttrs);
        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_SYLLABIFICATION, true);

        Set<SyllableConstituentType> hiddenConstituent = new HashSet<>();
        hiddenConstituent.add(SyllableConstituentType.SYLLABLESTRESSMARKER);
        hiddenConstituent.add(SyllableConstituentType.UNKNOWN);
        hiddenConstituent.add(SyllableConstituentType.WORDBOUNDARYMARKER);

        for (IPAElement p : ipaTranscript) {
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_PHONE, p);
            attrs.removeAttribute(StyleConstants.Foreground);
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE, true);
            attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE, true);
            if (p.getScType().equals(SyllableConstituentType.UNKNOWN)) {
                StyleConstants.setForeground(attrs, UIManager.getColor(TranscriptEditorUIProps.IPA_PAUSE));
            }
            retVal.add(doc.getBatchString(p.toString(), attrs));
            final SyllabificationInfo sInfo = p.getExtension(SyllabificationInfo.class);
            if (hiddenConstituent.contains(sInfo.getConstituentType())) continue;
            retVal.add(doc.getBatchString(":", attrs));
            attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_TRAVERSABLE);
            attrs.removeAttribute(TranscriptStyleConstants.ATTR_KEY_NOT_EDITABLE);
            if(sInfo.getConstituentType() == SyllableConstituentType.NUCLEUS && sInfo.isDiphthongMember()) {
                StyleConstants.setForeground(attrs, Color.RED);
                retVal.add(doc.getBatchString("D", attrs));
            }
            else {
                StyleConstants.setForeground(attrs, sInfo.getConstituentType().getColor());
                retVal.add(doc.getBatchString(String.valueOf(sInfo.getConstituentType().getIdChar()), attrs));
            }
        }
        attrs.removeAttribute(StyleConstants.Foreground);

        return retVal;
    }

    // endregion Getters and Setters
}
