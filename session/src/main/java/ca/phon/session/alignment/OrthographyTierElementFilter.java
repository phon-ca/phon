package ca.phon.session.alignment;

import ca.phon.orthography.*;
import ca.phon.orthography.Word;
import ca.phon.session.Tier;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class which describes how a tier is aligned with elements in the orthography.
 *
 */
public final class OrthographyTierElementFilter implements TierElementFilter {

    /**
     * Types in orthography which may be used in alignment
     */
    public static enum AlignableType {
        Action,
        Freecode,
        Group,
        Happening,
        InternalMedia,
        Linker,
        LongFeature,
        Nonvocal,
        OtherSpokenEvent,
        Pause,
        PhoneticGroup,
        Postcode,
        TagMarker,
        Terminator,
        Quotation,
        Word,
    };

    private final List<AlignableType> alignableTypes;

    public static final boolean DEFAULT_INCLUDE_WORD_XXX = false;
    private final boolean includeXXX;

    public static final boolean DEFAULT_INCLUDE_WORD_YYY = false;
    private final boolean includeYYY;

    public static final boolean DEFAULT_INCLUDE_WORD_WWW = false;
    private final boolean includeWWW;

    public static final boolean DEFAULT_INCLUDE_OMITTED = false;
    private final boolean includeOmitted;

    public static final boolean DEFAULT_INCLUDE_EXCLUDED = false;
    private final boolean includeExcluded;

    public OrthographyTierElementFilter() {
        this(Collections.singletonList(AlignableType.Word), DEFAULT_INCLUDE_WORD_XXX, DEFAULT_INCLUDE_WORD_YYY,
                DEFAULT_INCLUDE_WORD_WWW, DEFAULT_INCLUDE_OMITTED, DEFAULT_INCLUDE_EXCLUDED);
    }

    public OrthographyTierElementFilter(List<AlignableType> alignableTypes, boolean includeXXX, boolean includeYYY, boolean includeWWW, boolean includeOmitted, boolean includeExcluded) {
        this.alignableTypes = alignableTypes;
        this.includeXXX = includeXXX;
        this.includeYYY = includeYYY;
        this.includeWWW = includeWWW;
        this.includeOmitted = includeOmitted;
        this.includeExcluded = includeExcluded;
    }

    public List<OrthographyElement> filterOrthography(Orthography orthography) {
        final OrthographyFilter filter = new OrthographyFilter();
        orthography.accept(filter);
        return filter.elements;
    }

    public List<AlignableType> getAlignableTypes() {
        return Collections.unmodifiableList(this.alignableTypes);
    }

    public boolean isIncludeXXX() {
        return includeXXX;
    }

    public boolean isIncludeYYY() {
        return includeYYY;
    }

    public boolean isIncludeWWW() {
        return includeWWW;
    }

    public boolean isIncludeOmitted() {
        return includeOmitted;
    }

    public boolean isIncludeExcluded() {
        return includeExcluded;
    }

    public boolean isIncluded(AlignableType type) {
        return alignableTypes.contains(type);
    }

    @Override
    public List<?> filterTier(Tier<?> tier) {
        if(tier.getDeclaredType() != Orthography.class)
            return new ArrayList<>();
        return filterOrthography((Orthography) tier.getValue());
    }

    public final class OrthographyFilter extends AbstractOrthographyVisitor {

        final List<OrthographyElement> elements = new ArrayList<>();

        @Visits
        @Override
        public void visitWord(Word word) {
            boolean includeWord = isIncluded(AlignableType.Word);
            if(word.getPrefix() != null && word.getPrefix().getType() == WordType.OMISSION) {
                includeWord = isIncludeOmitted();
            }
            if(word.isUntranscribed()) {
                includeWord = switch (word.getUntranscribedType()) {
                    case UNINTELLIGIBLE -> isIncludeXXX();
                    case UNTRANSCRIBED -> isIncludeWWW();
                    case UNINTELLIGIBLE_WORD_WITH_PHO -> isIncludeYYY();
                };
            }
            if(includeWord)
                elements.add(word);
        }

        @Visits
        @Override
        public void visitCompoundWord(CompoundWord compoundWord) {
            visitWord(compoundWord);
        }

        @Visits
        @Override
        public void visitQuotation(Quotation quotation) {
            if(isIncluded(AlignableType.Quotation))
                elements.add(quotation);
        }

        @Visits
        @Override
        public void visitPause(Pause pause) {
            if(isIncluded(AlignableType.Pause))
                elements.add(pause);
        }

        @Visits
        @Override
        public void visitOrthoGroup(OrthoGroup group) {
            if(isIncluded(AlignableType.Group))
                elements.add(group);
            else {
                group.getElements().forEach(this::visit);
            }
        }

        @Visits
        @Override
        public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
            if(isIncluded(AlignableType.PhoneticGroup)) {
                elements.add(phoneticGroup);
            } else {
                phoneticGroup.getElements().forEach(this::visit);
            }
        }

        @Visits
        @Override
        public void visitTerminator(Terminator terminator) {
            if(isIncluded(AlignableType.Terminator))
                elements.add(terminator);
        }

        @Visits
        @Override
        public void visitLinker(Linker linker) {
            if(isIncluded(AlignableType.Linker))
                elements.add(linker);
        }

        @Visits
        @Override
        public void visitPostcode(Postcode postcode) {
            if(isIncluded(AlignableType.Postcode))
                elements.add(postcode);
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {
        }

    }

}
