package ca.phon.orthography;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public abstract class AbstractWordElementVisitor extends VisitorAdapter<WordElement> {

    @Visits
    public void visitText(WordText text) {}

    @Visits
    public void visitCaDelimiter(CaDelimiter caDelimiter) {}

    @Visits
    public void visitCaElement(CaElement caElement) {}

    @Visits
    public void visitLongFeature(LongFeature longFeature) {}

    @Visits
    public void visitOverlapPoint(OverlapPoint overlapPoint) {}

    @Visits
    public void visitProsody(Prosody prosody) {}

    @Visits
    public void vistShortening(Shortening shortening) {}

    @Visits
    public void visitCompoundWordMarker(CompoundWordMarker compoundWordMarker) {}

    @Visits
    public void visitUnderline(Underline underline) {}

    @Visits
    public void visitItalic(Italic italic) {}

}
