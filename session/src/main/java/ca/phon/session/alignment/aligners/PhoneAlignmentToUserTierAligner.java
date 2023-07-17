package ca.phon.session.alignment.aligners;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.util.Tuple;

import java.util.List;

public class PhoneAlignmentToUserTierAligner implements ITierAligner<PhoneAlignment, PhoneMap, UserTierData, UserTierElement> {

    @Override
    public TierAlignment<PhoneAlignment, PhoneMap, UserTierData, UserTierElement>
        calculateAlignment(Tier<PhoneAlignment> topTier, Tier<UserTierData> bottomTier, TierAlignmentRules alignmentRules) {
        final List<PhoneMap> alignments = topTier.getValue().getAlignments();
        final UserTierAlignmentFilter filter = new UserTierAlignmentFilter(alignmentRules);
        bottomTier.getValue().accept(filter);
        final List<UserTierElement> userTierElements = filter.getElements();
        final List<Tuple<PhoneMap, UserTierElement>> elements = TierAligner.mapAlignedElements(alignments, userTierElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
