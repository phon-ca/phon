package ca.phon.session.alignment;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.orthography.mor.*;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.UserTierType;
import ca.phon.util.Tuple;

import java.util.*;

public final class TierAligner {

    /**
     * Return a list of left-aligned tuples
     *
     * @param topElements
     * @param bottomElements
     * @return list of aligned tuples
     */
    public static List<Tuple<?, ?>> mapAlignedElements(List<?> topElements, List<?> bottomElements) {
        final List<Tuple<?, ?>> retVal = new ArrayList<>();
        final int n = Math.max(topElements.size(), bottomElements.size());
        for(int i = 0; i < n; i++) {
            final Object topElement = i < topElements.size() ? topElements.get(i) : null;
            final Object bottomElement = i < bottomElements.size() ? bottomElements.get(i) : null;
            final Tuple<?, ?> tuple = new Tuple<>(topElement, bottomElement);
            retVal.add(tuple);
        }
        return retVal;
    }

    public static TierAlignment alignTiers(Tier<?> topTier, Tier<?> bottomTier, TierAlignmentRules tierAlignmentRules) {
        final TierElementFilter tier1Filter = tierAlignmentRules.getFilterForTier(topTier.getName());
        final List<?> topElements = tier1Filter.filterTier(topTier);
        final TierElementFilter tier2Filter = tierAlignmentRules.getFilterForTier(bottomTier.getName());
        final List<?> bottomElements = tier2Filter.filterTier(bottomTier);
        return new TierAlignment(topTier, bottomTier, mapAlignedElements(topElements, bottomElements));
    }

    @SuppressWarnings("unchecked")
    public static TierAlignment alignTiers(Tier<?> topTier, Tier<?> bottomTier) {
        return alignTiers(topTier, bottomTier, TierAlignmentRules.defaultTierAlignmentRules(topTier, bottomTier));
    }

    /**
     * Calculates cross tier alignment for all tiers against the Orthography tier
     *
     * @param record
     * @return cross tier alignment for record against Orthography
     */
    public static CrossTierAlignment calculateCrossTierAlignment(Record record) {
        return calculateCrossTierAlignment(record, record.getOrthographyTier());
    }

    /**
     * Calculate cross tier alignment for all tiers which align by type
     *
     * @param record
     * @param topTier
     *
     * @return cross tier alignment for record
     */
    public static CrossTierAlignment calculateCrossTierAlignment(Record record, Tier<?> topTier) {
        Map<String, TierAlignment> alignmentMap = new LinkedHashMap<>();
        if(!topTier.isExcludeFromAlignment()) {
            if(topTier != record.getOrthographyTier())
                alignmentMap.put(SystemTierType.Orthography.getName(),
                        TierAligner.alignTiers(topTier, record.getOrthographyTier()));
            if(topTier != record.getIPATargetTier())
                alignmentMap.put(SystemTierType.IPATarget.getName(),
                        TierAligner.alignTiers(topTier, record.getIPATargetTier()));
            if(topTier != record.getIPAActualTier())
                alignmentMap.put(SystemTierType.IPAActual.getName(),
                        TierAligner.alignTiers(topTier, record.getIPAActualTier()));
            if(record.getPhoneAlignmentTier() != null) {
                if(topTier != record.getPhoneAlignmentTier())
                    alignmentMap.put(SystemTierType.PhoneAlignment.getName(),
                            TierAligner.alignTiers(topTier, record.getPhoneAlignmentTier()));
            }
            for(String tierName:record.getUserDefinedTierNames()) {
                final Tier<?> bottomTier = record.getTier(tierName);
                // handle morphology special cases
                if(topTier == record.getOrthographyTier() && bottomTier.getDeclaredType() == MorTierData.class) {
                    final UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
                    if(userTierType == UserTierType.Mor || userTierType == UserTierType.Trn) {
                        // align with orthography only
                        alignmentMap.put(tierName, TierAligner.alignTiers(topTier, bottomTier));
                    }
                }
                if(topTier == record.getOrthographyTier() && bottomTier.getDeclaredType() == GraspTierData.class) {
                    // align with morphology then orthography
                    final UserTierType graType = UserTierType.fromPhonTierName(tierName);
                    Tier<MorTierData> morTier = null;
                    if(graType == UserTierType.Gra) {
                        morTier = record.getTier(UserTierType.Mor.getTierName(), MorTierData.class);
                    } else if(graType == UserTierType.Grt) {
                        morTier = record.getTier(UserTierType.Trn.getTierName(), MorTierData.class);
                    }
                    if(morTier != null) {
                        final TierAlignment orthoMorAlignment = TierAligner.alignTiers(topTier, morTier);
                        final TierAlignment morGraAlignment = TierAligner.alignTiers(morTier, bottomTier);
                        final List<Tuple<?, ?>> orthoGraMap = new ArrayList<>();
                        for(Tuple alignedElements:orthoMorAlignment.getAlignedElements()) {
                            final OrthographyElement orthoEle = (OrthographyElement) alignedElements.getObj1();
                            final Mor morEle = (Mor) alignedElements.getObj2();
                            final List<Grasp> graEles = new ArrayList<>();
                            for(MorPre morPre:morEle.getMorPres()) {
                                final Grasp grasp = (Grasp) morGraAlignment.getAlignedElement(morPre);
                                if(grasp != null)
                                    graEles.add(grasp);
                            }
                            final Grasp grasp = (Grasp) morGraAlignment.getAlignedElement(morEle);
                            if(grasp != null)
                                graEles.add(grasp);
                            for(MorPost morPost:morEle.getMorPosts()) {
                                final Grasp gra = (Grasp) morGraAlignment.getAlignedElement(morPost);
                                if(gra != null)
                                    graEles.add(gra);
                            }

                            if(graEles.size() > 0) {
                                orthoGraMap.add(new Tuple<>(orthoEle, new GraspTierData(graEles)));
                            }
                        }
                        alignmentMap.put(tierName, new TierAlignment(topTier, bottomTier, orthoGraMap));
                    }
                }
                if(topTier.getDeclaredType() == MorTierData.class && bottomTier.getDeclaredType() == GraspTierData.class) {
                    final UserTierType morType = UserTierType.fromPhonTierName(topTier.getName());
                    final UserTierType graType = UserTierType.fromPhonTierName(bottomTier.getName());
                    if((morType == UserTierType.Mor && graType == UserTierType.Gra)
                        || (morType == UserTierType.Trn && graType == UserTierType.Grt)) {
                        alignmentMap.put(tierName, TierAligner.alignTiers(topTier, bottomTier));
                    }
                }
                if(topTier != bottomTier && bottomTier != null && bottomTier.hasValue() && !bottomTier.isExcludeFromAlignment()) {
                    alignmentMap.put(tierName, TierAligner.alignTiers(topTier, bottomTier));
                }
            }
        }
        return new CrossTierAlignment(topTier, alignmentMap);
    }

}
