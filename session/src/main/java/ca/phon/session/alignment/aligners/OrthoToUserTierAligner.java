package ca.phon.session.alignment.aligners;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.util.Tuple;

import java.util.List;

public class OrthoToUserTierAligner implements ITierAligner<Orthography, OrthographyElement, UserTierData, UserTierElement> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, UserTierData, UserTierElement>
        calculateAlignment(Tier<Orthography> topTier, Tier<UserTierData> bottomTier) {
        final OrthoAlignmentFilter orthoVisitor = new OrthoAlignmentFilter(bottomTier.getTierAlignmentRules());
        topTier.getValue().accept(orthoVisitor);
        final List<OrthographyElement> orthoElements = orthoVisitor.getAlignmentElements();
        final UserTierAlignmentFilter filter = new UserTierAlignmentFilter(bottomTier.getTierAlignmentRules());
        bottomTier.getValue().accept(filter);
        final List<UserTierElement> userTierElements = filter.getElements();
        final List<Tuple<OrthographyElement, UserTierElement>> elements = TierAligner.mapAlignedElements(orthoElements, userTierElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
