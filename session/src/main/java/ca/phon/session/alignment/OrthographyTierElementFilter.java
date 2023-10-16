package ca.phon.session.alignment;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
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
    public static final boolean DEFAULT_INCLUDE_WORD_YYY = false;
    public static final boolean DEFAULT_INCLUDE_WORD_WWW = false;
    public static final boolean DEFAULT_INCLUDE_OMITTED = false;
    public static final boolean DEFAULT_USE_REPLACEMENT = false;
    public static final boolean DEFAULT_INCLUDE_EXCLUDED = false;
    public static final boolean DEFAULT_INCLUDE_RETRACED = false;
    public static final boolean DEFAULT_INCLUDE_FRAGMENT = true;
    public static final boolean DEFAULT_INCLUDE_NONWORD = true;
    public static final boolean DEFAULT_INCLUDE_FILLER = true;
    public static final boolean DEFAULT_INCLUDE_ERROR = false;
    public record Options(boolean includeXXX, boolean includeYYY, boolean includeWWW,
                          boolean includeOmitted, boolean includeFrag, boolean includeNonword, boolean includeFiller,
                          boolean useReplacement, boolean includeExcluded, boolean includeRetraced, boolean includeError) { }
    private final Options options;

    public OrthographyTierElementFilter() {
        this(Collections.singletonList(AlignableType.Word),
                new Options(DEFAULT_INCLUDE_WORD_XXX, DEFAULT_INCLUDE_WORD_YYY, DEFAULT_INCLUDE_WORD_WWW,
                        DEFAULT_INCLUDE_OMITTED, DEFAULT_INCLUDE_FRAGMENT, DEFAULT_INCLUDE_NONWORD, DEFAULT_INCLUDE_FILLER,
                        DEFAULT_USE_REPLACEMENT, DEFAULT_INCLUDE_EXCLUDED, DEFAULT_INCLUDE_RETRACED, DEFAULT_INCLUDE_ERROR));
    }

    public OrthographyTierElementFilter(List<AlignableType> alignableTypes, Options options) {
        this.alignableTypes = alignableTypes;
        this.options = options;
    }

    public List<OrthographyElement> filterOrthography(Orthography orthography) {
        final OrthographyFilter filter = new OrthographyFilter();
        orthography.accept(filter);
        return filter.elements;
    }

    public List<AlignableType> getAlignableTypes() {
        return Collections.unmodifiableList(this.alignableTypes);
    }

    public Options getOptions() {
        return this.options;
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
            if(options.useReplacement && word.getReplacements().size() > 0) {
                // align with first replacement
                final Replacement replacement = word.getReplacements().get(0);
                replacement.getWords().forEach(this::visit);
            } else {
                boolean includeWord = isIncluded(AlignableType.Word);
                if(word.getPrefix() != null) {
                    includeWord = switch (word.getPrefix().getType()) {
                        case OMISSION -> options.includeOmitted;
                        case FRAGMENT -> options.includeFrag;
                        case FILLER -> options.includeFiller;
                        case NONWORD -> options.includeNonword;
                    };
                }
                if (word.isUntranscribed()) {
                    includeWord = switch (word.getUntranscribedType()) {
                        case UNINTELLIGIBLE -> options.includeXXX;
                        case UNTRANSCRIBED -> options.includeWWW;
                        case UNINTELLIGIBLE_WORD_WITH_PHO -> options.includeYYY;
                    };
                }
                if (includeWord)
                    elements.add(word);
            }
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
            boolean isError = false;
            boolean isRetraced = false;
            boolean isExcluded = false;
            for(OrthographyAnnotation annotation:group.getAnnotations()) {
                if(annotation instanceof Error)
                    isError = true;
                else if(annotation instanceof Marker marker) {
                    switch (marker.getType()) {
                        case RETRACING, RETRACING_REFORMULATION, RETRACING_UNCLEAR, RETRACING_WITH_CORRECTION, FALSE_START:
                            isRetraced = true;
                            break;

                        case EXCLUDE:
                            isExcluded = true;
                            break;

                        default:
                            break;
                    }
                }
            }
            if(isError && !options.includeError) return;
            if(isRetraced && !options.includeRetraced) return;
            if(isExcluded && !options.includeExcluded) return;
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

        @Visits
        @Override
        public void visitTagMarker(TagMarker tagMarker) {
            if(isIncluded(AlignableType.TagMarker))
                elements.add(tagMarker);
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {
        }

    }

}
