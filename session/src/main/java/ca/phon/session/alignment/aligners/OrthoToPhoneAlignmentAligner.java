package ca.phon.session.alignment.aligners;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.PhoneAlignment;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.util.Tuple;

import java.util.List;

public class OrthoToPhoneAlignmentAligner implements ITierAligner<Orthography, OrthographyElement, PhoneAlignment, PhoneMap> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, PhoneAlignment, PhoneMap>
        calculateAlignment(Tier<Orthography> topTier, Tier<PhoneAlignment> bottomTier, TierAlignmentRules alignmentRules) {
        final OrthoAlignmentFilter orthoVisitor = new OrthoAlignmentFilter(alignmentRules);
        topTier.getValue().accept(orthoVisitor);
        final List<OrthographyElement> orthoElements = orthoVisitor.getAlignmentElements();
        final List<PhoneMap> alignments = bottomTier.getValue().getAlignments();
        final List<Tuple<OrthographyElement, PhoneMap>> elements = TierAligner.mapAlignedElements(orthoElements, alignments);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
