package ca.phon.session.alignment;

import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            alignmentMap.put(SystemTierType.Orthography.getName(),
                    TierAligner.alignTiers(topTier, record.getOrthographyTier()));
            alignmentMap.put(SystemTierType.IPATarget.getName(),
                    TierAligner.alignTiers(topTier, record.getIPATargetTier()));
            alignmentMap.put(SystemTierType.IPAActual.getName(),
                    TierAligner.alignTiers(topTier, record.getIPAActualTier()));
            if(record.getPhoneAlignmentTier() != null) {
                alignmentMap.put(SystemTierType.PhoneAlignment.getName(),
                        TierAligner.alignTiers(topTier, record.getPhoneAlignmentTier()));
            }
            for(String tierName:record.getUserDefinedTierNames()) {
                final Tier<?> bottomTier = record.getTier(tierName);
                if(bottomTier != null && bottomTier.hasValue() && !bottomTier.isExcludeFromAlignment()) {
                    alignmentMap.put(tierName, TierAligner.alignTiers(topTier, bottomTier));
                }
            }
        }
        return new CrossTierAlignment(topTier, alignmentMap);
    }

}
