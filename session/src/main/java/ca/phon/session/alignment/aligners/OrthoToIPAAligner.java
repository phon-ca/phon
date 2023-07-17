package ca.phon.session.alignment.aligners;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.util.Tuple;

import java.util.List;

public class OrthoToIPAAligner implements ITierAligner<Orthography, OrthographyElement, IPATranscript, IPATranscript> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, IPATranscript, IPATranscript>
        calculateAlignment(Tier<Orthography> topTier, Tier<IPATranscript> bottomTier, TierAlignmentRules alignmentRules) {
        final OrthoAlignmentFilter orthoVisitor = new OrthoAlignmentFilter(alignmentRules);
        topTier.getValue().accept(orthoVisitor);
        final List<OrthographyElement> orthoElements = orthoVisitor.getAlignmentElements();
        final List<IPATranscript> ipaElements = bottomTier.getValue().words().stream().filter(new IPAAlignmentFilter(alignmentRules)).toList();

        final List<Tuple<OrthographyElement, IPATranscript>> elements = TierAligner.mapAlignedElements(orthoElements, ipaElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
