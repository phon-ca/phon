package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.usertier.UserTierElement;
import ca.phon.util.Tuple;

import java.util.List;

public class IPAToUserTierAligner implements ITierAligner<IPATranscript, IPATranscript, UserTierData, UserTierElement> {

    @Override
    public TierAlignment<IPATranscript, IPATranscript, UserTierData, UserTierElement> calculateAlignment(Tier<IPATranscript> topTier, Tier<UserTierData> bottomTier) {
        final List<IPATranscript> ipaWords = topTier.getValue().words();
        final UserTierAlignmentFilter filter = new UserTierAlignmentFilter(bottomTier.getTierAlignmentRules());
        bottomTier.getValue().accept(filter);
        final List<UserTierElement> userTierElements = filter.getElements();
        final List<Tuple<IPATranscript, UserTierElement>> elements = TierAligner.mapAlignedElements(ipaWords, userTierElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
