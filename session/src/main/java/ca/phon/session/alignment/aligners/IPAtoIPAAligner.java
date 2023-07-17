package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.util.Tuple;

import java.util.List;

public class IPAtoIPAAligner implements ITierAligner<IPATranscript, IPATranscript, IPATranscript, IPATranscript> {

    @Override
    public TierAlignment<IPATranscript, IPATranscript, IPATranscript, IPATranscript>
        calculateAlignment(Tier<IPATranscript> topTier, Tier<IPATranscript> bottomTier, TierAlignmentRules alignmentRules) {
        final IPAAlignmentFilter filter = new IPAAlignmentFilter(alignmentRules);
        final List<IPATranscript> topElements = topTier.getValue().words().stream().filter(filter).toList();
        final List<IPATranscript> bottomElements = bottomTier.getValue().words().stream().filter(filter).toList();
        final List<Tuple<IPATranscript, IPATranscript>> elements = TierAligner.mapAlignedElements(topElements, bottomElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
