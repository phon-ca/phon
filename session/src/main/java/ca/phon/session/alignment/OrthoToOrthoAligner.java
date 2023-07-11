package ca.phon.session.alignment;

import ca.phon.orthography.*;
import ca.phon.session.Tier;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

public class OrthoToOrthoAligner
        implements TierAligner<Orthography, OrthographyElement, Orthography, OrthographyElement> {

    @Override
    public TierAlignment<Orthography, OrthographyElement, Orthography, OrthographyElement>
        calculateAlignment(Tier<Orthography> topTier, Tier<Orthography> bottomTier) {

        final OrthoAlignmentVisitor topVisitor = new OrthoAlignmentVisitor(bottomTier.getTierAlignmentRules());
        topTier.getValue().accept(topVisitor);
        final List<OrthographyElement> topElements = topVisitor.getAlignmentElements();

        final OrthoAlignmentVisitor bottomVisitor = new OrthoAlignmentVisitor(topTier.getTierAlignmentRules());
        bottomTier.getValue().accept(bottomVisitor);
        final List<OrthographyElement> bottomElements = bottomVisitor.getAlignmentElements();

        int n = Math.max(topElements.size(), bottomElements.size());
        for(int i = 0; i < n; i++) {
            
        }

        return new TierAlignment<Orthography, OrthographyElement, Orthography, OrthographyElement>(topTier, bottomTier, new ArrayList<>());
    }

}
