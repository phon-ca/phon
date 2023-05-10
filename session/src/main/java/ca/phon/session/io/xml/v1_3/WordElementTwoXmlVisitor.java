package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.xml.*;
import ca.phon.session.Word;
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
        final XMLOrthographyCaDelimiter xmlCaDelimiter = factory.createXMLOrthographyCaDelimiter();
        final XMLOrthographyBeginEndType beginEndType = switch (caDelimiter.getBeginEnd()) {
            case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
            case END -> XMLOrthographyBeginEndType.END;
        };
        xmlCaDelimiter.setType(beginEndType);
        final XMLOrthographyCaDelimiterType type = switch (caDelimiter.getType()) {
            case BREATHY_VOICE -> XMLOrthographyCaDelimiterType.BREATHY_VOICE;
            case CREAKY -> XMLOrthographyCaDelimiterType.CREAKY;
            case FASTER -> XMLOrthographyCaDelimiterType.FASTER;
            case HIGH_PITCH -> XMLOrthographyCaDelimiterType.HIGH_PITCH;
            case LOUDER -> XMLOrthographyCaDelimiterType.LOUDER;
            case LOW_PITCH -> XMLOrthographyCaDelimiterType.LOW_PITCH;
            case PRECISE -> XMLOrthographyCaDelimiterType.PRECISE;
            case REPEATED_SEGMENT -> XMLOrthographyCaDelimiterType.REPEATED_SEGMENT;
            case SINGING -> XMLOrthographyCaDelimiterType.SINGING;
            case SLOWER -> XMLOrthographyCaDelimiterType.SLOWER;
            case SMILE_VOICE -> XMLOrthographyCaDelimiterType.SMILE_VOICE;
            case SOFTER -> XMLOrthographyCaDelimiterType.SOFTER;
            case UNSURE -> XMLOrthographyCaDelimiterType.UNSURE;
            case WHISPER -> XMLOrthographyCaDelimiterType.WHISPER;
            case YAWN -> XMLOrthographyCaDelimiterType.YAWN;
        };
        xmlCaDelimiter.setLabel(type);
        wordElements.add(xmlCaDelimiter);
    }

    @Override
    @Visits
    public void visitCaElement(CaElement caElement) {
        final XMLOrthographyCaElement xmlCaElement = factory.createXMLOrthographyCaElement();
        final XMLOrthographyCaElementType type = switch (caElement.getType()) {
            case BLOCKED_SEGMENTS -> XMLOrthographyCaElementType.BLOCKED_SEGMENTS;
            case CONSTRICTION -> XMLOrthographyCaElementType.CONSTRICTION;
            case INHALATION -> XMLOrthographyCaElementType.INHALATION;
            case LAUGH_IN_WORD -> XMLOrthographyCaElementType.LAUGH_IN_WORD;
            case PITCH_DOWN -> XMLOrthographyCaElementType.PITCH_DOWN;
            case PITCH_RESET -> XMLOrthographyCaElementType.PITCH_RESET;
            case PITCH_UP -> XMLOrthographyCaElementType.PITCH_UP;
            case PRIMARY_STRESS -> XMLOrthographyCaElementType.PRIMARY_STRESS;
            case SECONDARY_STRESS -> XMLOrthographyCaElementType.SECONDARY_STRESS;
        };
        xmlCaElement.setType(type);
        wordElements.add(xmlCaElement);
    }

    @Override
    @Visits
    public void visitLongFeature(LongFeature longFeature) {
        final XMLOrthographyLongFeature xmlLongFeature = factory.createXMLOrthographyLongFeature();
        final XMLOrthographyBeginEndType beginEndType = switch (longFeature.getBeginEnd()) {
            case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
            case END -> XMLOrthographyBeginEndType.END;
        };
        xmlLongFeature.setType(beginEndType);
        xmlLongFeature.setValue(longFeature.getLabel());
        wordElements.add(xmlLongFeature);
    }

    @Override
    @Visits
    public void visitOverlapPoint(OverlapPoint overlapPoint) {
        final XMLOrthographyStartEndType startEnd =
                overlapPoint.getType() == OverlapPointType.TOP_START || overlapPoint.getType() == OverlapPointType.BOTTOM_START
                ? XMLOrthographyStartEndType.START : XMLOrthographyStartEndType.END;
        final XMLOrthographyTopBottomType topBottom =
                overlapPoint.getType() == OverlapPointType.TOP_START || overlapPoint.getType() == OverlapPointType.TOP_END
                ? XMLOrthographyTopBottomType.TOP : XMLOrthographyTopBottomType.BOTTOM;
        final XMLOrthographyOverlapPoint xmlOverlapPt = factory.createXMLOrthographyOverlapPoint();
        xmlOverlapPt.setTopBottom(topBottom);
        xmlOverlapPt.setStartEnd(startEnd);
        if(overlapPoint.getIndex() >= 0)
            xmlOverlapPt.setIndex(BigInteger.valueOf(overlapPoint.getIndex()));
        wordElements.add(xmlOverlapPt);
    }

    @Override
    @Visits
    public void visitProsody(Prosody prosody) {
        final XMLOrthographyP xmlP = factory.createXMLOrthographyP();
        final XMLOrthographyProsodyType xmlType = switch (prosody.getType()) {
            case BLOCKING -> XMLOrthographyProsodyType.BLOCKING;
            case DRAWL -> XMLOrthographyProsodyType.DRAWL;
            case PAUSE -> XMLOrthographyProsodyType.PAUSE;
        };
        xmlP.setType(xmlType);
        wordElements.add(xmlP);
    }

    @Override
    @Visits
    public void vistShortening(Shortening shortening) {
        final XMLOrthographyShortening xmlShortening = factory.createXMLOrthographyShortening();
        xmlShortening.setValue(shortening.getOrthoText().text());
        wordElements.add(xmlShortening);
    }

    @Override
    @Visits
    public void visitCompoundWordMarker(CompoundWordMarker compoundWordMarker) {
        final XMLOrthographyWk xmlWk = factory.createXMLOrthographyWk();
        final XMLOrthographyWkType type = switch (compoundWordMarker.getType()) {
            case CLITIC -> XMLOrthographyWkType.CLI;
            case COMPOUND -> XMLOrthographyWkType.CMP;
        };
        xmlWk.setType(type);
        wordElements.add(xmlWk);
    }

    @Override
    @Visits
    public void visitUnderline(Underline underline) {
        final XMLOrthographyUnderline xmlUnderline = factory.createXMLOrthographyUnderline();
        final XMLOrthographyBeginEndType beginEndType = switch (underline.getBeginEnd()) {
            case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
            case END -> XMLOrthographyBeginEndType.END;
        };
        xmlUnderline.setType(beginEndType);
        wordElements.add(xmlUnderline);
    }

    @Override
    @Visits
    public void visitItalic(Italic italic) {
        final XMLOrthographyItalic xmlItalic = factory.createXMLOrthographyItalic();
        final XMLOrthographyBeginEndType beginEndType = switch (italic.getBeginEnd()) {
            case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
            case END -> XMLOrthographyBeginEndType.END;
        };
        xmlItalic.setType(beginEndType);
        wordElements.add(xmlItalic);
    }

    @Override
    public void fallbackVisit(WordElement obj) {

    }

}
