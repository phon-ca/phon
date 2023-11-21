package ca.phon.session.io.xml;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.*;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.Mor;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.UserTierType;
import ca.phon.session.alignment.CrossTierAlignment;
import ca.phon.session.alignment.TierAligner;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A container use for OneToOne alignment between morphology and IPA tiers with the mail line (Orthography.)
 * If present when saving xml, morphology and IPA tiers present in the OneToOne object will be added to the
 * appropriate word, pg, tag marker, etc. element in the xml.  This class is used for compatibility with TalkBank.
 */
public class OneToOne {

    private Map<String, Mor> morTierData;

    private Map<String, GraspTierData> graspTierData;

    private Map<String, IPATranscript> ipaTierData;

    public OneToOne(Map<String, Mor> morTierData, Map<String, GraspTierData> graspTierData, Map<String, IPATranscript> ipaTierData) {
        super();
        this.morTierData = morTierData;
        this.graspTierData = graspTierData;
        this.ipaTierData = ipaTierData;
    }

    public Map<String, Mor> getMorTierData() {
        return Collections.unmodifiableMap(morTierData);
    }

    public Map<String, GraspTierData> getGraspTierData() {
        return Collections.unmodifiableMap(graspTierData);
    }

    public Map<String, IPATranscript> getIpaTierData() {
        return Collections.unmodifiableMap(ipaTierData);
    }

    /**
     * Apply OneToOne annotation to relevant objects before xml output.
     *
     * @param record
     *
     */
    public static void annotateRecord(Record record) {
        final CrossTierAlignment xTierAlignment = TierAligner.calculateCrossTierAlignment(record);
        final OrthographyAnnotator annotator = new OrthographyAnnotator(xTierAlignment);
        record.getOrthographyTier().getValue().accept(annotator);
    }

    /**
     * Remove any OneToOne annotations on given record
     *
     * @param record
     */
    public static void removeAnnotations(Record record) {
        final OrthographyAnnotator annotator = new OrthographyAnnotator();
        record.getOrthographyTier().getValue().accept(annotator);
    }

    /**
     * Add or remove OneToOne annotations to a record
     */
    public static class OrthographyAnnotator extends VisitorAdapter<OrthographyElement> {

        private final boolean removeAnnotations;

        private final CrossTierAlignment xTierAlignment;

        /**
         * Creates a visitor which remove OneToOne annotations
         */
        public OrthographyAnnotator() {
            this.xTierAlignment = null;
            this.removeAnnotations = true;
        }

        /**
         * Create a visitor which adds OneToOne annotations
         * based on given cross tier alignment
         * @param xTierAlignment
         */
        public OrthographyAnnotator(CrossTierAlignment xTierAlignment) {
            this.xTierAlignment = xTierAlignment;
            this.removeAnnotations = false;
        }

        private void visitAlignableElement(OrthographyElement orthographyElement) {
            if(removeAnnotations) {
                orthographyElement.putExtension(OneToOne.class, null);
            } else {
                final Map<String, Object> alignedElements = xTierAlignment.getAlignedElements(orthographyElement);

                final Map<String, Mor> morMap = new LinkedHashMap<>();
                final Map<String, GraspTierData> graMap = new LinkedHashMap<>();
                final Map<String, IPATranscript> ipaMap = new LinkedHashMap<>();
                // check for mor tiers and their corresponding grasp tiers
                if (alignedElements.containsKey(UserTierType.Mor.getPhonTierName())) {
                    final Mor mor = (Mor) alignedElements.get(UserTierType.Mor.getPhonTierName());
                    morMap.put(UserTierType.Mor.getPhonTierName(), mor);
                }
                if (alignedElements.containsKey(UserTierType.Gra.getPhonTierName())) {
                    final GraspTierData grasp = (GraspTierData) alignedElements.get(UserTierType.Gra.getPhonTierName());
                    graMap.put(UserTierType.Gra.getPhonTierName(), grasp);
                }
                if (alignedElements.containsKey(UserTierType.Trn.getPhonTierName())) {
                    final Mor mor = (Mor) alignedElements.get(UserTierType.Trn.getPhonTierName());
                    morMap.put(UserTierType.Trn.getPhonTierName(), mor);
                }
                if (alignedElements.containsKey(UserTierType.Grt.getPhonTierName())) {
                    final GraspTierData grasp = (GraspTierData) alignedElements.get(UserTierType.Grt.getPhonTierName());
                    graMap.put(UserTierType.Grt.getPhonTierName(), grasp);
                }

                // ipa tiers
                if (alignedElements.containsKey(SystemTierType.IPATarget.getName())) {
                    final IPATranscript ipa = (IPATranscript) alignedElements.get(SystemTierType.IPATarget.getName());
                    ipaMap.put(SystemTierType.IPATarget.getName(), ipa);
                }
                if (alignedElements.containsKey(SystemTierType.IPAActual.getName())) {
                    final IPATranscript ipa = (IPATranscript) alignedElements.get(SystemTierType.IPAActual.getName());
                    ipaMap.put(SystemTierType.IPAActual.getName(), ipa);
                }

                if (!morMap.isEmpty() || !graMap.isEmpty() || !ipaMap.isEmpty()) {
                    final OneToOne oneToOne = new OneToOne(morMap, graMap, ipaMap);
                    orthographyElement.putExtension(OneToOne.class, oneToOne);
                }
            }
        }

        @Visits
        public void visitWord(Word word) {
            visitAlignableElement(word);

            if(word.getReplacements().size() > 0) {
                for(Replacement replacement:word.getReplacements()) {
                    replacement.getWords().forEach(this::visitAlignableElement);
                }
            }
        }

        @Visits
        public void visitOrthoGroup(OrthoGroup group) {
            group.getElements().forEach(this::visit);
        }

        @Visits
        public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
            visitAlignableElement(phoneticGroup);
            phoneticGroup.getElements().forEach(this::visit);
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {
            visitAlignableElement(obj);
        }
    }

}
