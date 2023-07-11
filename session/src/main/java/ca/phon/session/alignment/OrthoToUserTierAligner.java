package ca.phon.session.alignment;

import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.Participant;
import ca.phon.session.Tier;
import ca.phon.session.usertier.TierString;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.session.usertier.UserTierInternalMedia;
import ca.phon.util.Tuple;

import java.util.List;

public class OrthoToUserTierAligner implements ITierAligner<Orthography, OrthographyElement, UserTierData, UserTierElement> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, UserTierData, UserTierElement>
        calculateAlignment(Tier<Orthography> topTier, Tier<UserTierData> bottomTier) {
        final OrthoAlignmentFilter orthoVisitor = new OrthoAlignmentFilter(bottomTier.getTierAlignmentRules());
        topTier.getValue().accept(orthoVisitor);
        final List<OrthographyElement> orthoElements = orthoVisitor.getAlignmentElements();
        final List<UserTierElement> userTierElements = bottomTier.getValue().stream()
            .filter(ele -> keepUserTierElement(bottomTier.getTierAlignmentRules(), ele))
            .toList();
        final List<Tuple<OrthographyElement, UserTierElement>> elements = TierAligner.mapAlignedElements(orthoElements, userTierElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

    private boolean keepUserTierElement(TierAlignmentRules rules, UserTierElement ele) {
        if(ele instanceof TierString) {
            return rules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Word);
        } else if(ele instanceof UserTierInternalMedia) {
            return rules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.InternalMedia);
        } else {
            return false;
        }
    }

}
