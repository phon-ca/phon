package ca.phon.session.alignment.aligners;

import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.util.Tuple;

import java.util.List;

public class UserTierToUserTierAligner implements ITierAligner<UserTierData, UserTierElement, UserTierData, UserTierElement> {

    @Override
    public TierAlignment<UserTierData, UserTierElement, UserTierData, UserTierElement> calculateAlignment(Tier<UserTierData> topTier, Tier<UserTierData> bottomTier) {
        final UserTierAlignmentFilter filter1 = new UserTierAlignmentFilter(topTier.getTierAlignmentRules());
        topTier.getValue().accept(filter1);
        final List<UserTierElement> topElements = filter1.getElements();
        final UserTierAlignmentFilter filter2 = new UserTierAlignmentFilter(bottomTier.getTierAlignmentRules());
        bottomTier.getValue().accept(filter2);
        final List<UserTierElement> bottomElements = filter2.getElements();
        final List<Tuple<UserTierElement, UserTierElement>> elements = TierAligner.mapAlignedElements(topElements, bottomElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
