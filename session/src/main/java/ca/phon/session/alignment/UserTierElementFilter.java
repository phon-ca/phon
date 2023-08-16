package ca.phon.session.alignment;

import ca.phon.session.Tier;
import ca.phon.session.usertier.*;
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
        if(tier.getDeclaredType() != UserTierData.class)
            throw new IllegalArgumentException();
        final UserTierAlignmentFilter alignmentFilter = new UserTierAlignmentFilter();
        UserTierData tierData = (UserTierData) tier.getValue();
        tierData.accept(alignmentFilter);
        return alignmentFilter.getElements();
    }

    public class UserTierAlignmentFilter extends VisitorAdapter<UserTierElement> {

        private final List<UserTierElement> elements = new ArrayList<>();

        @Visits
        public void visitTierString(TierString tierString) {
            if(isIncluded(UserTierElementFilter.AlignableType.Type))
                elements.add(tierString);
        }

        @Visits
        public void visitInternalMedia(UserTierInternalMedia internalMedia) {
            if(isIncluded(UserTierElementFilter.AlignableType.InternalMedia))
                elements.add(internalMedia);
        }

        @Visits
        public void visitComment(UserTierComment comment) {
            if(isIncluded(UserTierElementFilter.AlignableType.Comment))
                elements.add(comment);
        }

        @Override
        public void fallbackVisit(UserTierElement obj) {

        }

        public List<UserTierElement> getElements() {
            return elements;
        }

    }

}
