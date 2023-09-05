package ca.phon.session.alignment;

import ca.phon.session.Tier;
import ca.phon.session.tierdata.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserTierElementFilter implements TierElementFilter {

    public static enum AlignableType {
        Type,
        InternalMedia,
        Comment;
    };

    private final List<AlignableType> alignableTypes;

    public UserTierElementFilter() {
        this(List.of(AlignableType.Type));
    }

    public UserTierElementFilter(List<AlignableType> alignableTypes) {
        this.alignableTypes = alignableTypes;
    }

    public List<AlignableType> getAlignableTypes() {
        return Collections.unmodifiableList(alignableTypes);
    }

    public boolean isIncluded(AlignableType alignableType) {
        return this.alignableTypes.contains(alignableType);
    }

    @Override
    public List<?> filterTier(Tier<?> tier) {
        if(tier.getDeclaredType() != TierData.class)
            throw new IllegalArgumentException();
        final UserTierAlignmentFilter alignmentFilter = new UserTierAlignmentFilter();
        TierData tierData = (TierData) tier.getValue();
        tierData.accept(alignmentFilter);
        return alignmentFilter.getElements();
    }

    public class UserTierAlignmentFilter extends VisitorAdapter<TierElement> {

        private final List<TierElement> elements = new ArrayList<>();

        @Visits
        public void visitTierString(TierString tierString) {
            if(isIncluded(UserTierElementFilter.AlignableType.Type))
                elements.add(tierString);
        }

        @Visits
        public void visitInternalMedia(TierInternalMedia internalMedia) {
            if(isIncluded(UserTierElementFilter.AlignableType.InternalMedia))
                elements.add(internalMedia);
        }

        @Visits
        public void visitComment(TierComment comment) {
            if(isIncluded(UserTierElementFilter.AlignableType.Comment))
                elements.add(comment);
        }

        @Override
        public void fallbackVisit(TierElement obj) {

        }

        public List<TierElement> getElements() {
            return elements;
        }

    }

}
