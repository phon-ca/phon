package ca.phon.session.alignment;

import ca.phon.orthography.*;
import ca.phon.session.Tier;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

/**
 * Element filter for the <code>%wor</code> tier.  Provides a list of word+interval pairs.
 */
public class WorElementFilter extends VisitorAdapter<OrthographyElement> implements TierElementFilter {

    private List<Orthography> wordIntervalPairs;

    private OrthographyBuilder builder = new OrthographyBuilder();

    public WorElementFilter() {
        super();
        this.wordIntervalPairs = new ArrayList<>();
    }

    @Visits
    public void visitWord(Word word) {
        builder.append(word);
    }

    @Visits
    public void visitCompoundWord(CompoundWord word) {
        builder.append(word);
    }

    @Visits
    public void visitPhoneticGroup(PhoneticGroup group) {
        builder.append(group);
    }

    @Visits
    public void visitOrthoGroup(OrthoGroup group) {
        for(OrthographyElement element : group.getElements()) {
            super.visit(element);
        }
    }

    @Visits
    public void visitInternalMedia(InternalMedia media) {
        builder.append(media);
        final Orthography wordIntervalPair = builder.toOrthography();
        this.wordIntervalPairs.add(wordIntervalPair);
        builder.clear();
    }

    @Override
    public List<?> filterTier(Tier<?> tier) {
        this.wordIntervalPairs.clear();
        if(tier.getDeclaredType() != Orthography.class) return List.of();
        final Orthography value = (Orthography)tier.getValue();
        value.accept(this);
        return wordIntervalPairs;
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {

    }
}
