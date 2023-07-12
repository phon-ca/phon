package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.util.Tuple;

import java.util.List;

public class IPAtoIPAAligner implements ITierAligner<IPATranscript, IPATranscript, IPATranscript, IPATranscript> {

    @Override
    public TierAlignment<IPATranscript, IPATranscript, IPATranscript, IPATranscript> calculateAlignment(Tier<IPATranscript> topTier, Tier<IPATranscript> bottomTier) {
        final List<Tuple<IPATranscript, IPATranscript>> elements = TierAligner.mapAlignedElements(topTier.getValue().words(), bottomTier.getValue().words());
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
