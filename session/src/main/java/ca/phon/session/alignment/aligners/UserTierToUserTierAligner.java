package ca.phon.session.alignment.aligners;

import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.util.Tuple;

import java.util.List;

public class UserTierToUserTierAligner implements ITierAligner<UserTierData, UserTierElement, UserTierData, UserTierElement> {

    @Override
    public TierAlignment<UserTierData, UserTierElement, UserTierData, UserTierElement>
        calculateAlignment(Tier<UserTierData> topTier, Tier<UserTierData> bottomTier, TierAlignmentRules alignmentRules) {
        final UserTierAlignmentFilter filter1 = new UserTierAlignmentFilter(alignmentRules);
        topTier.getValue().accept(filter1);
        final List<UserTierElement> topElements = filter1.getElements();
        final UserTierAlignmentFilter filter2 = new UserTierAlignmentFilter(alignmentRules);
        bottomTier.getValue().accept(filter2);
        final List<UserTierElement> bottomElements = filter2.getElements();
        final List<Tuple<UserTierElement, UserTierElement>> elements = TierAligner.mapAlignedElements(topElements, bottomElements);

        // setup sub-type alignments
        if(topTier.getTierAlignmentRules().getType() == TierAlignmentRules.TierAlignmentType.ByTypeThenSubType
            && bottomTier.getTierAlignmentRules().getType() == TierAlignmentRules.TierAlignmentType.ByTypeThenSubType) {
            // TODO
        }

        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
