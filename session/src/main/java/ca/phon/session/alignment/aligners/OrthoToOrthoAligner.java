package ca.phon.session.alignment.aligners;

import ca.phon.orthography.*;
import ca.phon.session.Tier;
import ca.phon.session.alignment.ITierAligner;
import ca.phon.session.alignment.TierAligner;
import ca.phon.session.alignment.TierAlignment;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.util.Tuple;

import java.util.List;

public class OrthoToOrthoAligner
        implements ITierAligner<Orthography, OrthographyElement, Orthography, OrthographyElement> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, Orthography, OrthographyElement>
        calculateAlignment(Tier<Orthography> topTier, Tier<Orthography> bottomTier, TierAlignmentRules alignmentRules) {
        final OrthoAlignmentFilter topVisitor = new OrthoAlignmentFilter(alignmentRules);
        topTier.getValue().accept(topVisitor);
        final List<OrthographyElement> topElements = topVisitor.getAlignmentElements();

        final OrthoAlignmentFilter bottomVisitor = new OrthoAlignmentFilter(alignmentRules);
        bottomTier.getValue().accept(bottomVisitor);
        final List<OrthographyElement> bottomElements = bottomVisitor.getAlignmentElements();

        final List<Tuple<OrthographyElement, OrthographyElement>> elements = TierAligner.mapAlignedElements(topElements, bottomElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
