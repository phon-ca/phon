package ca.phon.session.alignment;

import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.util.Tuple;

import java.util.*;

/**
 * Alignment between a target/top tier and one or more other tiers.
 *
 */
public class CrossTierAlignment {

    /**
     * Calculate alignment for all tiers which align by type
     *
     * @param record
     * @param topTier
     *
     * @return cross tier alignment for record
     */
    public static CrossTierAlignment calculateAlignment(Record record, Tier<?> topTier) {
        Map<String, TierAlignment<?,?,?,?>> alignmentMap = new LinkedHashMap<>();
        alignmentMap.put(SystemTierType.Orthography.getName(),
                TierAligner.alignTiers(topTier, record.getOrthographyTier()));
        alignmentMap.put(SystemTierType.IPATarget.getName(),
                TierAligner.alignTiers(topTier, record.getIPATargetTier()));
        alignmentMap.put(SystemTierType.IPAActual.getName(),
                TierAligner.alignTiers(topTier, record.getIPAActualTier()));
        alignmentMap.put(SystemTierType.PhoneAlignment.getName(),
                TierAligner.alignTiers(topTier, record.getPhoneAlignmentTier()));
        for(String tierName:record.getUserDefinedTierNames()) {
            alignmentMap.put(tierName, TierAligner.alignTiers(topTier, record.getTier(tierName)));
        }
        return new CrossTierAlignment(topTier, alignmentMap);
    }

    private final Tier<?> topTier;

    private final Map<String, TierAlignment<?, ?, ?, ?>> tierAlignments;

    public CrossTierAlignment(Tier<?> topTier, Map<String, TierAlignment<?,?,?,?>> alignmentMap) {
        super();
        this.topTier = topTier;
        this.tierAlignments = alignmentMap;
    }

    /**
     * Return a map of alignments for the given object
     *
     * @param obj an object from the top tier
     */
    public Map<String, Object> getAlignedElements(Object obj) {
        Map<String, Object> retVal = new LinkedHashMap<>();
        for(String tierName:tierAlignments.keySet()) {
            final TierAlignment<?,?,?,?> tierAlignment = tierAlignments.get(tierName);
            if(tierAlignment != null) {
                Optional<?> alignedEle = tierAlignment.getAlignedElements().stream()
                        .filter(ae -> ae.getObj1() == obj).map(Tuple::getObj2).findAny();
                if(alignedEle.isPresent()) {
                    retVal.put(tierName, alignedEle.get());
                }
            }
        }
        return retVal;
    }

    /**
     * Returns a list of all top alignment elements across all tiers
     *
     * @return a list of all top alignment elements in the cross tier alignment
     */
    public List<Object> getTopAlignmentElements() {
        List<Object> retVal = new ArrayList<>();
        for(String alignedTier: tierAlignments.keySet()) {
            for(var alignedElements:tierAlignments.get(alignedTier).getAlignedElements()) {
                if(!retVal.contains(alignedElements.getObj1()))
                    retVal.add(alignedElements.getObj1());
            }
        }
        // TODO sort by string index
        return retVal;
    }

    /**
     * Get tier alignment for specified tier
     *
     * @param tierName
     */
    public TierAlignment<?,?,?,?> getTierAlignment(String tierName) {
        return tierAlignments.get(tierName);
    }

    /**
     * Returns a list of all bottom alignment elements for the specified tier
     *
     * @param tierName
     * @return alignment elements for tier
     */
    public List<Object> getBottomAlignmentElements(String tierName) {
        final TierAlignment<?,?,?,?> tierAlignment = tierAlignments.get(tierName);
        List<Object> retVal = new ArrayList<>();
        if(tierAlignment != null)
            retVal.addAll(tierAlignment.getAlignedElements().stream().map(Tuple::getObj2).toList());
        return retVal;
    }

}
