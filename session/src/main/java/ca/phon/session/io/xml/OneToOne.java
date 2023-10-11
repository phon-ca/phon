package ca.phon.session.io.xml;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.*;
import ca.phon.orthography.mor.Grasp;
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

    public static class OrthographyAnnotator extends VisitorAdapter<OrthographyElement> {

        private final CrossTierAlignment xTierAlignment;

        public OrthographyAnnotator(CrossTierAlignment xTierAlignment) {
            this.xTierAlignment = xTierAlignment;
        }

        private void visitAlignableElement(OrthographyElement orthographyElement) {
            final Map<String, Object> alignedElements = xTierAlignment.getAlignedElements(orthographyElement);

            final Map<String, Mor> morMap = new LinkedHashMap<>();
            final Map<String, GraspTierData> graMap = new LinkedHashMap<>();
            final Map<String, IPATranscript> ipaMap = new LinkedHashMap<>();
            // check for mor tiers
            if(alignedElements.containsKey(UserTierType.Mor.getTierName())) {
                final Mor mor = (Mor)alignedElements.get(UserTierType.Mor.getTierName());
                morMap.put(UserTierType.Mor.getTierName(), mor);
            }
            if(alignedElements.containsKey(UserTierType.Gra.getTierName())) {
                final GraspTierData grasp = (GraspTierData) alignedElements.get(UserTierType.Gra.getTierName());
                graMap.put(UserTierType.Gra.getTierName(), grasp);
            }
            if(alignedElements.containsKey(UserTierType.Trn.getTierName())) {
                final Mor mor = (Mor)alignedElements.get(UserTierType.Trn.getTierName());
                morMap.put(UserTierType.Trn.getTierName(), mor);
            }
            if(alignedElements.containsKey(UserTierType.Grt.getTierName())) {
                final GraspTierData grasp = (GraspTierData) alignedElements.get(UserTierType.Grt.getTierName());
                graMap.put(UserTierType.Grt.getTierName(), grasp);
            }
            // ipa tiers


            if(!morMap.isEmpty() || !graMap.isEmpty() || ipaMap.size() > 0) {
                final OneToOne oneToOne = new OneToOne(morMap, graMap, ipaMap);
                orthographyElement.putExtension(OneToOne.class, oneToOne);
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
