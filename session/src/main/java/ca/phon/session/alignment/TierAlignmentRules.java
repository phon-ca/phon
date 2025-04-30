package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.Tier;
import ca.phon.session.UserTierType;
import ca.phon.session.tierdata.TierData;
import ca.phon.util.Tuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross tier alignment rules for a user-defined tier.  These rules define which elements
 * from each tier are included in the alignment.  If noalign is true, the user defined tier
 * is not included in cross tier alignment at all.
 *
 */
public final class TierAlignmentRules {

    /**
     * Create default tier alignment rules for given tiers.
     *
     * @param tier1
     * @param tier2
     *
     * @return default tier alignment rules for given tiers
     */
    public static TierAlignmentRules defaultTierAlignmentRules(Tier<?> tier1, Tier<?> tier2) {
        final TierElementFilter filter1 = TierElementFilter.filterForAlignedTiers(tier1, tier2);
        final TierElementFilter filter2 = TierElementFilter.filterForAlignedTiers(tier2, tier1);
        return new TierAlignmentRules(tier1.getName(), filter1, tier2.getName(), filter2);
    }

    private final Map<String, TierElementFilter> tierElementFilterMap = new LinkedHashMap<>();

    public TierAlignmentRules(String tier1, TierElementFilter filter1,
                              String tier2, TierElementFilter filter2) {
        super();
        this.tierElementFilterMap.put(tier1, filter1);
        this.tierElementFilterMap.put(tier2, filter2);
    }

    public Tuple<String, String> getTierNames() {
        var nameList = tierElementFilterMap.keySet().stream().toList();
        return new Tuple<>(nameList.get(0), nameList.get(1));
    }

    /**
     * Return element filter for given tierName, null if not in found
     *
     * @param tierName
     * @return element filter or null
     */
    public TierElementFilter getFilterForTier(String tierName) {
        return tierElementFilterMap.get(tierName);
    }

}
