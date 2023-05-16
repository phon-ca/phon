package ca.phon.session;

import ca.phon.orthography.*;
import ca.phon.orthography.Word;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which describes how a tier is aligned with elements in the orthography.
 *
 */
public class WordAlignmentRules {

    public static final boolean DEFAULT_INCLUDE_WORD_XXX = false;
    private boolean includeXXX = DEFAULT_INCLUDE_WORD_XXX;

    public static final boolean DEFAULT_INCLUDE_WORD_YYY = false;
    private boolean includeYYY = DEFAULT_INCLUDE_WORD_YYY;

    public static final boolean DEFAULT_INCLUDE_WORD_WWW = false;
    private boolean includeWWW = DEFAULT_INCLUDE_WORD_WWW;

    public static final boolean DEFAULT_INCLUDE_OMITTED = false;
    private boolean includeOmitted = DEFAULT_INCLUDE_OMITTED;

    public static final boolean DEFAULT_INCLUDE_EXCLUDED = false;
    private boolean includeExcluded = DEFAULT_INCLUDE_EXCLUDED;

    public static final boolean DEFAULT_INCLUDE_PAUSES = true;
    private boolean includePauses = DEFAULT_INCLUDE_PAUSES;

    public static final boolean DEFAULT_INCLUDE_PHONETIC_GROUPS = false;
    private boolean includePhoneticGroups = DEFAULT_INCLUDE_PHONETIC_GROUPS;

    public static final boolean DEFAULT_INCLUDE_TERMINATORS = false;
    private boolean includeTerminators = DEFAULT_INCLUDE_TERMINATORS;

    public WordAlignmentRules() {
        super();
    }

    public WordAlignmentRules(boolean includeWords, boolean includeXXX, boolean includeYYY, boolean includeWWW, boolean includeOmitted, boolean includeExcluded, boolean includePauses, boolean includePhoneticGroups, boolean includeTerminators) {
        this.includeXXX = includeXXX;
        this.includeYYY = includeYYY;
        this.includeWWW = includeWWW;
        this.includeOmitted = includeOmitted;
        this.includeExcluded = includeExcluded;
        this.includePauses = includePauses;
        this.includePhoneticGroups = includePhoneticGroups;
        this.includeTerminators = includeTerminators;
    }

    public List<OrthographyElement> filterOrthography(Orthography orthography) {
        final OrthographyFilter filter = new OrthographyFilter();
        orthography.accept(filter);
        return filter.elements;
    }

    public boolean isIncludeXXX() {
        return includeXXX;
    }

    public void setIncludeXXX(boolean includeXXX) {
        this.includeXXX = includeXXX;
    }

    public boolean isIncludeYYY() {
        return includeYYY;
    }

    public void setIncludeYYY(boolean includeYYY) {
        this.includeYYY = includeYYY;
    }

    public boolean isIncludeWWW() {
        return includeWWW;
    }

    public void setIncludeWWW(boolean includeWWW) {
        this.includeWWW = includeWWW;
    }

    public boolean isIncludeOmitted() {
        return includeOmitted;
    }

    public void setIncludeOmitted(boolean includeOmitted) {
        this.includeOmitted = includeOmitted;
    }

    public boolean isIncludeExcluded() {
        return includeExcluded;
    }

    public void setIncludeExcluded(boolean includeExcluded) {
        this.includeExcluded = includeExcluded;
    }

    public boolean isIncludePauses() {
        return includePauses;
    }

    public void setIncludePauses(boolean includePauses) {
        this.includePauses = includePauses;
    }

    public boolean isIncludePhoneticGroups() {
        return includePhoneticGroups;
    }

    public void setIncludePhoneticGroups(boolean includePhoneticGroups) {
        this.includePhoneticGroups = includePhoneticGroups;
    }

    public boolean isIncludeTerminators() {
        return includeTerminators;
    }

    public void setIncludeTerminators(boolean includeTerminators) {
        this.includeTerminators = includeTerminators;
    }

    private final class OrthographyFilter extends AbstractOrthographyVisitor {

        final List<OrthographyElement> elements = new ArrayList<>();

        @Visits
        @Override
        public void visitWord(Word word) {
            boolean includeWord = true;
            if(word.getPrefix().getType() == WordType.OMISSION) {
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
        public void visitPause(Pause pause) {
            if(isIncludePauses())
                elements.add(pause);
        }

        @Override
        public void visitOrthoGroup(OrthoGroup group) {
            group.getElements().forEach(this::visit);
        }

        @Override
        public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
            if(isIncludePhoneticGroups()) {
                elements.add(phoneticGroup);
            } else {
                phoneticGroup.getElements().forEach(this::visit);
            }
        }

        @Override
        public void visitTerminator(Terminator terminator) {
            if(isIncludeTerminators())
                elements.add(terminator);
        }

        @Override
        public void fallbackVisit(OrthographyElement obj) {

        }

    }

}
