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

    private final Tier<?> topTier;

    private final Map<String, TierAlignment> tierAlignments;

    public CrossTierAlignment(Tier<?> topTier, Map<String, TierAlignment> alignmentMap) {
        super();
        this.topTier = topTier;
        this.tierAlignments = alignmentMap;
    }

    public Tier<?> getTopTier() {
        return topTier;
    }

    /**
     * Return a map of alignments for the given object
     *
     * @param obj an object from the top tier
     */
    public Map<String, Object> getAlignedElements(Object obj) {
        Map<String, Object> retVal = new LinkedHashMap<>();
        for(String tierName:tierAlignments.keySet()) {
            final TierAlignment tierAlignment = tierAlignments.get(tierName);
            if(tierAlignment != null) {
                Optional<? extends Tuple<?, ?>> alignedEle = tierAlignment.getAlignedElements().stream()
                        .filter(ae -> ae.getObj1() == obj).findAny();
                if(alignedEle.isPresent() && alignedEle.get().getObj2() != null) {
                    retVal.put(tierName, alignedEle.get().getObj2());
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
                boolean include = true;
                for(Object existingValue:retVal) {
                    if(existingValue == alignedElements.getObj1()) {
                        include = false;
                        break;
                    }
                }
                if(include)
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
    public TierAlignment getTierAlignment(String tierName) {
        return tierAlignments.get(tierName);
    }

    /**
     * Returns a list of all bottom alignment elements for the specified tier
     *
     * @param tierName
     * @return alignment elements for tier
     */
    public List<Object> getBottomAlignmentElements(String tierName) {
        final TierAlignment tierAlignment = tierAlignments.get(tierName);
        List<Object> retVal = new ArrayList<>();
        if(tierAlignment != null)
            retVal.addAll(tierAlignment.getAlignedElements().stream().map(Tuple::getObj2).toList());
        return retVal;
    }

}
