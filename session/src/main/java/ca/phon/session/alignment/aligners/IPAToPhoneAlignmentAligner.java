package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.util.Tuple;

import java.util.List;

public class IPAToPhoneAlignmentAligner implements ITierAligner<IPATranscript, IPATranscript, PhoneAlignment, PhoneMap> {

    @Override
    public TierAlignment<IPATranscript, IPATranscript, PhoneAlignment, PhoneMap>
        calculateAlignment(Tier<IPATranscript> topTier, Tier<PhoneAlignment> bottomTier, TierAlignmentRules alignmentRules) {
        final List<IPATranscript> ipaElements = topTier.getValue().words().stream().filter(new IPAAlignmentFilter(alignmentRules)).toList();
        final List<Tuple<IPATranscript, PhoneMap>> elements = TierAligner.mapAlignedElements(ipaElements, bottomTier.getValue().getAlignments());
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
