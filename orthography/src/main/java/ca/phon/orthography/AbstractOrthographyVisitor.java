package ca.phon.orthography;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Base class for OrthographyElement visitor which has a default
 * implementation for every object which implements {@link OrthographyElement}
 */
public abstract class AbstractOrthographyVisitor extends VisitorAdapter<OrthographyElement> {

    @Visits
    public void visitLinker(Linker linker) {}

    @Visits
    public void visitCompoundWord(CompoundWord compoundWord) {}

    @Visits
    public void visitWord(Word word) {}

    @Visits
    public void visitOrthoGroup(OrthoGroup group) {}

    @Visits
    public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {}

    @Visits
    public void visitQuotation(Quotation quotation) {}

    @Visits
    public void visitPause(Pause pause) {}

    @Visits
    public void visitInternalMedia(InternalMedia internalMedia) {}

    @Visits
    public void visitFreecode(Freecode freecode) {}

    @Visits
    public void visitEvent(Event event) {}

    @Visits
    public void visitSeparator(Separator separator) {}

    @Visits
    public void visitToneMarker(ToneMarker toneMarker) {}

    @Visits
    public void visitTagMarker(TagMarker tagMarker) {}

    @Visits
    public void visitOverlapPoint(OverlapPoint overlapPoint) {}

    @Visits
    public void visitUnderline(Underline underline) {}

    @Visits
    public void visitItalic(Italic italic) {}

    @Visits
    public void visitLongFeature(LongFeature longFeature) {}

    @Visits
    public void visitNonvocal(Nonvocal nonvocal) {}

    @Visits
    public void visitTerminator(Terminator terminator) {}

    @Visits
    public void visitPostcode(Postcode postcode) {}

}
