package ca.phon.session.alignment;

import ca.phon.orthography.*;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

class OrthoAlignmentVisitor extends AbstractOrthographyVisitor {

    private final TierAlignmentRules alignmentRules;

    private final List<OrthographyElement> elements;

    public OrthoAlignmentVisitor(TierAlignmentRules rules) {
        this.alignmentRules = rules;
        this.elements = new ArrayList<>();
    }

    @Visits
    @Override
    public void visitLinker(Linker linker) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Linker))
            this.elements.add(linker);
    }

    @Visits
    @Override
    public void visitCompoundWord(CompoundWord compoundWord) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Word))
            this.elements.add(compoundWord);
    }

    @Visits
    @Override
    public void visitWord(Word word) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Word)) {
            if (word.getPrefix().getType() == WordType.OMISSION && !alignmentRules.getWordAlignmentRules().isIncludeOmitted())
                return;
            if (word.getExtension(Marker.class) != null && word.getExtension(Marker.class).getType() == MarkerType.EXCLUDE
                    && !alignmentRules.getWordAlignmentRules().isIncludeExcluded())
                return;
            if (word.getWord().equals("xxx") && !alignmentRules.getWordAlignmentRules().isIncludeXXX())
                return;
            if (word.getWord().equals("yyy") && !alignmentRules.getWordAlignmentRules().isIncludeYYY())
                return;
            if (word.getWord().equals("www") && !alignmentRules.getWordAlignmentRules().isIncludeWWW())
                return;
            this.elements.add(word);
        }
    }

    @Visits
    @Override
    public void visitOrthoGroup(OrthoGroup group) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Group)) {
            this.elements.add(group);
        } else {
            group.getElements().forEach(this::visit);
        }
    }

    @Override
    public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.PhoneticGroup)) {
            this.elements.add(phoneticGroup);
        } else {
            phoneticGroup.getElements().forEach(this::visit);
        }
    }

    @Visits
    @Override
    public void visitPause(Pause pause) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Pause)) {
            this.elements.add(pause);
        }
    }

    @Visits
    @Override
    public void visitInternalMedia(InternalMedia internalMedia) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.InternalMedia)) {
            this.elements.add(internalMedia);
        }
    }

    @Visits
    @Override
    public void visitFreecode(Freecode freecode) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Freecode)) {
            this.elements.add(freecode);
        }
    }

    @Visits
    @Override
    public void visitAction(Action action) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Action)) {
            this.elements.add(action);
        }
    }

    @Visits
    @Override
    public void visitHappening(Happening happening) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Happening)) {
            this.elements.add(happening);
        }
    }

    @Visits
    @Override
    public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.OtherSpokenEvent)) {
            this.elements.add(otherSpokenEvent);
        }
    }

    @Visits
    @Override
    public void visitTagMarker(TagMarker tagMarker) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.TagMarker)) {
            this.elements.add(tagMarker);
        }
    }

    @Visits
    @Override
    public void visitLongFeature(LongFeature longFeature) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.LongFeature)) {
            this.elements.add(longFeature);
        }
    }

    @Visits
    @Override
    public void visitNonvocal(Nonvocal nonvocal) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Nonvocal)) {
            this.elements.add(nonvocal);
        }
    }

    @Visits
    @Override
    public void visitTerminator(Terminator terminator) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Terminator)) {
            this.elements.add(terminator);
        }
    }

    @Visits
    @Override
    public void visitPostcode(Postcode postcode) {
        if (alignmentRules.getWordAlignmentRules().isIncluded(TypeAlignmentRules.AlignableType.Postcode)) {
            this.elements.add(postcode);
        }
    }

    @Override
    public void fallbackVisit(OrthographyElement obj) {

    }

    public List<OrthographyElement> getAlignmentElements() {
        return this.elements;
    }

}
