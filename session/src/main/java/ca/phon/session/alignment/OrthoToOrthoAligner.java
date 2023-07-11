package ca.phon.session.alignment;

import ca.phon.orthography.*;
import ca.phon.session.Tier;
import ca.phon.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class OrthoToOrthoAligner
        implements ITierAligner<Orthography, OrthographyElement, Orthography, OrthographyElement> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, Orthography, OrthographyElement>
        calculateAlignment(Tier<Orthography> topTier, Tier<Orthography> bottomTier) {

        final OrthoAlignmentFilter topVisitor = new OrthoAlignmentFilter(bottomTier.getTierAlignmentRules());
        topTier.getValue().accept(topVisitor);
        final List<OrthographyElement> topElements = topVisitor.getAlignmentElements();

        final OrthoAlignmentFilter bottomVisitor = new OrthoAlignmentFilter(topTier.getTierAlignmentRules());
        bottomTier.getValue().accept(bottomVisitor);
        final List<OrthographyElement> bottomElements = bottomVisitor.getAlignmentElements();

        final List<Tuple<OrthographyElement, OrthographyElement>> elements = TierAligner.mapAlignedElements(topElements, bottomElements);
        return new TierAlignment<>(topTier, bottomTier, elements);
    }

}
