package ca.phon.session.alignment;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.Tier;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class OrthoToIPAAligner implements ITierAligner<Orthography, OrthographyElement, IPATranscript, IPATranscript> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, IPATranscript, IPATranscript> calculateAlignment(Tier<Orthography> topTier, Tier<IPATranscript> bottomTier) {
        final OrthoAlignmentFilter orthoVisitor = new OrthoAlignmentFilter(bottomTier.getTierAlignmentRules());
        topTier.getValue().accept(orthoVisitor);
        final List<OrthographyElement> orthoElements = orthoVisitor.getAlignmentElements();
        final List<IPATranscript> ipaElements = bottomTier.getValue().words();
        final List<Tuple<OrthographyElement, IPATranscript>> elements = TierAligner.mapAlignedElements(orthoElements, ipaElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
