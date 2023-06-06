package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.CaDelimiter;
import ca.phon.orthography.CaElement;
import ca.phon.orthography.Italic;
import ca.phon.orthography.LongFeature;
import ca.phon.orthography.OverlapPoint;
import ca.phon.orthography.Shortening;
import ca.phon.orthography.Underline;
import ca.phon.session.io.xml.v13.*;
import ca.phon.visitor.annotation.Visits;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class WordElementTwoXmlVisitor extends AbstractWordElementVisitor {

    private final ObjectFactory factory = new ObjectFactory();

    private final List<Object> wordElements;

    public WordElementTwoXmlVisitor() {
        this(new ArrayList<>());
    }

    public WordElementTwoXmlVisitor(List<Object> wordElements) {
        this.wordElements = wordElements;
    }

    @Override
    @Visits
    public void visitText(WordText text) {
        wordElements.add(text.text());
    }

    @Override
    @Visits
    public void visitCaDelimiter(CaDelimiter caDelimiter) {
        final ca.phon.session.io.xml.v13.CaDelimiter xmlCaDelimiter = factory.createCaDelimiter();
        final BeginEndType beginEndType = switch (caDelimiter.getBeginEnd()) {
            case BEGIN -> BeginEndType.BEGIN;
            case END -> BeginEndType.END;
        };
        xmlCaDelimiter.setType(beginEndType);
        final ca.phon.session.io.xml.v13.CaDelimiterType type = switch (caDelimiter.getType()) {
            case BREATHY_VOICE -> ca.phon.session.io.xml.v13.CaDelimiterType.BREATHY_VOICE;
            case CREAKY -> ca.phon.session.io.xml.v13.CaDelimiterType.CREAKY;
            case FASTER -> ca.phon.session.io.xml.v13.CaDelimiterType.FASTER;
            case HIGH_PITCH -> ca.phon.session.io.xml.v13.CaDelimiterType.HIGH_PITCH;
            case LOUDER -> ca.phon.session.io.xml.v13.CaDelimiterType.LOUDER;
            case LOW_PITCH -> ca.phon.session.io.xml.v13.CaDelimiterType.LOW_PITCH;
            case PRECISE -> ca.phon.session.io.xml.v13.CaDelimiterType.PRECISE;
            case REPEATED_SEGMENT -> ca.phon.session.io.xml.v13.CaDelimiterType.REPEATED_SEGMENT;
            case SINGING -> ca.phon.session.io.xml.v13.CaDelimiterType.SINGING;
            case SLOWER -> ca.phon.session.io.xml.v13.CaDelimiterType.SLOWER;
            case SMILE_VOICE -> ca.phon.session.io.xml.v13.CaDelimiterType.SMILE_VOICE;
            case SOFTER -> ca.phon.session.io.xml.v13.CaDelimiterType.SOFTER;
            case UNSURE -> ca.phon.session.io.xml.v13.CaDelimiterType.UNSURE;
            case WHISPER -> ca.phon.session.io.xml.v13.CaDelimiterType.WHISPER;
            case YAWN -> ca.phon.session.io.xml.v13.CaDelimiterType.YAWN;
        };
        xmlCaDelimiter.setLabel(type);
        wordElements.add(xmlCaDelimiter);
    }

    @Override
    @Visits
    public void visitCaElement(CaElement caElement) {
        final ca.phon.session.io.xml.v13.CaElement xmlCaElement = factory.createCaElement();
        final ca.phon.session.io.xml.v13.CaElementType type = switch (caElement.getType()) {
            case BLOCKED_SEGMENTS -> ca.phon.session.io.xml.v13.CaElementType.BLOCKED_SEGMENTS;
            case CONSTRICTION -> ca.phon.session.io.xml.v13.CaElementType.CONSTRICTION;
            case INHALATION -> ca.phon.session.io.xml.v13.CaElementType.INHALATION;
            case LAUGH_IN_WORD -> ca.phon.session.io.xml.v13.CaElementType.LAUGH_IN_WORD;
            case PITCH_DOWN -> ca.phon.session.io.xml.v13.CaElementType.PITCH_DOWN;
            case PITCH_RESET -> ca.phon.session.io.xml.v13.CaElementType.PITCH_RESET;
            case PITCH_UP -> ca.phon.session.io.xml.v13.CaElementType.PITCH_UP;
            case PRIMARY_STRESS -> ca.phon.session.io.xml.v13.CaElementType.PRIMARY_STRESS;
            case SECONDARY_STRESS -> ca.phon.session.io.xml.v13.CaElementType.SECONDARY_STRESS;
        };
        xmlCaElement.setType(type);
        wordElements.add(xmlCaElement);
    }

    @Override
    @Visits
    public void visitLongFeature(LongFeature longFeature) {
        final ca.phon.session.io.xml.v13.LongFeature xmlLongFeature = factory.createLongFeature();
        final BeginEndType beginEndType = switch (longFeature.getBeginEnd()) {
            case BEGIN -> BeginEndType.BEGIN;
            case END -> BeginEndType.END;
        };
        xmlLongFeature.setType(beginEndType);
        xmlLongFeature.setValue(longFeature.getLabel());
        wordElements.add(xmlLongFeature);
    }

    @Override
    @Visits
    public void visitOverlapPoint(OverlapPoint overlapPoint) {
        final StartEndType startEnd =
                overlapPoint.getType() == OverlapPointType.TOP_START || overlapPoint.getType() == OverlapPointType.BOTTOM_START
                ? StartEndType.START : StartEndType.END;
        final TopBottomType topBottom =
                overlapPoint.getType() == OverlapPointType.TOP_START || overlapPoint.getType() == OverlapPointType.TOP_END
                ? TopBottomType.TOP : TopBottomType.BOTTOM;
        final ca.phon.session.io.xml.v13.OverlapPoint xmlOverlapPt = factory.createOverlapPoint();
        xmlOverlapPt.setTopBottom(topBottom);
        xmlOverlapPt.setStartEnd(startEnd);
        if(overlapPoint.getIndex() >= 0)
            xmlOverlapPt.setIndex(BigInteger.valueOf(overlapPoint.getIndex()));
        wordElements.add(xmlOverlapPt);
    }

    @Override
    @Visits
    public void visitProsody(Prosody prosody) {
        final P xmlP = factory.createP();
        final ca.phon.session.io.xml.v13.ProsodyType xmlType = switch (prosody.getType()) {
            case BLOCKING -> ca.phon.session.io.xml.v13.ProsodyType.BLOCKING;
            case DRAWL -> ca.phon.session.io.xml.v13.ProsodyType.DRAWL;
            case PAUSE -> ca.phon.session.io.xml.v13.ProsodyType.PAUSE;
        };
        xmlP.setType(xmlType);
        wordElements.add(xmlP);
    }

    @Override
    @Visits
    public void vistShortening(Shortening shortening) {
        final ca.phon.session.io.xml.v13.Shortening xmlShortening = factory.createShortening();
        xmlShortening.setValue(shortening.getOrthoText().text());
        wordElements.add(xmlShortening);
    }

    @Override
    @Visits
    public void visitCompoundWordMarker(CompoundWordMarker compoundWordMarker) {
        final Wk xmlWk = factory.createWk();
        final WkType type = switch (compoundWordMarker.getType()) {
            case CLITIC -> WkType.CLI;
            case COMPOUND -> WkType.CMP;
        };
        xmlWk.setType(type);
        wordElements.add(xmlWk);
    }

    @Override
    @Visits
    public void visitUnderline(Underline underline) {
        final ca.phon.session.io.xml.v13.Underline xmlUnderline = factory.createUnderline();
        final BeginEndType beginEndType = switch (underline.getBeginEnd()) {
            case BEGIN -> BeginEndType.BEGIN;
            case END -> BeginEndType.END;
        };
        xmlUnderline.setType(beginEndType);
        wordElements.add(xmlUnderline);
    }

    @Override
    @Visits
    public void visitItalic(Italic italic) {
        final ca.phon.session.io.xml.v13.Italic xmlItalic = factory.createItalic();
        final BeginEndType beginEndType = switch (italic.getBeginEnd()) {
            case BEGIN -> BeginEndType.BEGIN;
            case END -> BeginEndType.END;
        };
        xmlItalic.setType(beginEndType);
        wordElements.add(xmlItalic);
    }

    @Override
    public void fallbackVisit(WordElement obj) {

    }

}
