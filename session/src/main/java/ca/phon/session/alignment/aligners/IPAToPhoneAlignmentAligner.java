package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.util.Tuple;

import java.util.List;

public class IPAToPhoneAlignmentAligner implements ITierAligner<IPATranscript, IPATranscript, PhoneAlignment, PhoneMap> {

    @Override
    public TierAlignment<IPATranscript, IPATranscript, PhoneAlignment, PhoneMap> calculateAlignment(Tier<IPATranscript> topTier, Tier<PhoneAlignment> bottomTier) {
        final List<Tuple<IPATranscript, PhoneMap>> elements = TierAligner.mapAlignedElements(topTier.getValue().words(), bottomTier.getValue().getAlignments());
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
