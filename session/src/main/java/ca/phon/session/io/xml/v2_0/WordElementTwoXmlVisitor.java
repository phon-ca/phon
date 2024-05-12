package ca.phon.session.io.xml.v2_0;

import ca.phon.orthography.*;
import ca.phon.orthography.CaDelimiter;
import ca.phon.orthography.CaElement;
import ca.phon.orthography.Italic;
import ca.phon.orthography.LongFeature;
import ca.phon.orthography.OverlapPoint;
import ca.phon.orthography.Shortening;
import ca.phon.orthography.Underline;
import ca.phon.visitor.annotation.Visits;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class WordElementTwoXmlVisitor extends AbstractWordElementVisitor {

    private final ObjectFactory factory = new ObjectFactory();

    private final List<Serializable> wordElements;

    public WordElementTwoXmlVisitor() {
        this(new ArrayList<>());
    }

    public WordElementTwoXmlVisitor(List<Serializable> wordElements) {
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
        final XmlCaDelimiterType xmlCaDelimiter = factory.createXmlCaDelimiterType();
        final XmlBeginEndType beginEndType = switch (caDelimiter.getBeginEnd()) {
            case BEGIN -> XmlBeginEndType.BEGIN;
            case END -> XmlBeginEndType.END;
        };
        xmlCaDelimiter.setType(beginEndType);
        final XmlCaDelimiterTypeType type = switch (caDelimiter.getType()) {
            case BREATHY_VOICE -> XmlCaDelimiterTypeType.BREATHY_VOICE;
            case CREAKY -> XmlCaDelimiterTypeType.CREAKY;
            case FASTER -> XmlCaDelimiterTypeType.FASTER;
            case HIGH_PITCH -> XmlCaDelimiterTypeType.HIGH_PITCH;
            case LOUDER -> XmlCaDelimiterTypeType.LOUDER;
            case LOW_PITCH -> XmlCaDelimiterTypeType.LOW_PITCH;
            case PRECISE -> XmlCaDelimiterTypeType.PRECISE;
            case REPEATED_SEGMENT -> XmlCaDelimiterTypeType.REPEATED_SEGMENT;
            case SINGING -> XmlCaDelimiterTypeType.SINGING;
            case SLOWER -> XmlCaDelimiterTypeType.SLOWER;
            case SMILE_VOICE -> XmlCaDelimiterTypeType.SMILE_VOICE;
            case SOFTER -> XmlCaDelimiterTypeType.SOFTER;
            case UNSURE -> XmlCaDelimiterTypeType.UNSURE;
            case WHISPER -> XmlCaDelimiterTypeType.WHISPER;
            case YAWN -> XmlCaDelimiterTypeType.YAWN;
        };
        xmlCaDelimiter.setLabel(type);
        wordElements.add(factory.createCaDelimiter(xmlCaDelimiter));
    }

    @Override
    @Visits
    public void visitCaElement(CaElement caElement) {
        final XmlCaElementType xmlCaElement = factory.createXmlCaElementType();
        final XmlCaElementTypeType type = switch (caElement.getType()) {
            case BLOCKED_SEGMENTS -> XmlCaElementTypeType.BLOCKED_SEGMENTS;
            case CONSTRICTION -> XmlCaElementTypeType.CONSTRICTION;
            case INHALATION -> XmlCaElementTypeType.INHALATION;
            case LAUGH_IN_WORD -> XmlCaElementTypeType.LAUGH_IN_WORD;
            case PITCH_DOWN -> XmlCaElementTypeType.PITCH_DOWN;
            case PITCH_RESET -> XmlCaElementTypeType.PITCH_RESET;
            case PITCH_UP -> XmlCaElementTypeType.PITCH_UP;
            case PRIMARY_STRESS -> XmlCaElementTypeType.PRIMARY_STRESS;
            case SECONDARY_STRESS -> XmlCaElementTypeType.SECONDARY_STRESS;
            case HARDENING -> XmlCaElementTypeType.HARDENING;
            case HURRIED_START -> XmlCaElementTypeType.HURRIED_START;
            case SUDDEN_STOP -> XmlCaElementTypeType.SUDDEN_STOP;
        };
        xmlCaElement.setType(type);
        wordElements.add(factory.createCaElement(xmlCaElement));
    }

    @Override
    @Visits
    public void visitLongFeature(LongFeature longFeature) {
        final XmlLongFeatureType xmlLongFeature = factory.createXmlLongFeatureType();
        final XmlBeginEndType beginEndType = switch (longFeature.getBeginEnd()) {
            case BEGIN -> XmlBeginEndType.BEGIN;
            case END -> XmlBeginEndType.END;
        };
        xmlLongFeature.setType(beginEndType);
        xmlLongFeature.setContent(longFeature.getLabel());
        wordElements.add(factory.createLongFeature(xmlLongFeature));
    }

    @Override
    @Visits
    public void visitOverlapPoint(OverlapPoint overlapPoint) {
        final XmlStartEndType startEnd =
                overlapPoint.getType() == OverlapPointType.TOP_START || overlapPoint.getType() == OverlapPointType.BOTTOM_START
                ? XmlStartEndType.START : XmlStartEndType.END;
        final XmlTopBottomType topBottom =
                overlapPoint.getType() == OverlapPointType.TOP_START || overlapPoint.getType() == OverlapPointType.TOP_END
                ? XmlTopBottomType.TOP : XmlTopBottomType.BOTTOM;
        final XmlOverlapPointType xmlOverlapPt = factory.createXmlOverlapPointType();
        xmlOverlapPt.setTopBottom(topBottom);
        xmlOverlapPt.setStartEnd(startEnd);
        if(overlapPoint.getIndex() >= 0)
            xmlOverlapPt.setIndex(BigInteger.valueOf(overlapPoint.getIndex()));
        wordElements.add(factory.createOverlapPoint(xmlOverlapPt));
    }

    @Override
    @Visits
    public void visitProsody(Prosody prosody) {
        final XmlProsodyType xmlP = factory.createXmlProsodyType();
        final XmlProsodyTypeType xmlType = switch (prosody.getType()) {
            case DRAWL -> XmlProsodyTypeType.DRAWL;
            case PAUSE -> XmlProsodyTypeType.PAUSE;
        };
        xmlP.setType(xmlType);
        wordElements.add(factory.createP(xmlP));
    }

    @Override
    @Visits
    public void vistShortening(Shortening shortening) {
        final XmlShorteningType xmlShortening = factory.createXmlShorteningType();
        xmlShortening.setValue(shortening.getOrthoText().text());
        wordElements.add(factory.createShortening(xmlShortening));
    }

    @Override
    @Visits
    public void visitCompoundWordMarker(CompoundWordMarker compoundWordMarker) {
        final XmlCompoundType xmlWk = factory.createXmlCompoundType();
        final XmlWkType type = switch (compoundWordMarker.getType()) {
            case CLITIC -> XmlWkType.CLI;
            case COMPOUND -> XmlWkType.CMP;
        };
        xmlWk.setType(type);
        wordElements.add(factory.createWk(xmlWk));
    }

    @Override
    @Visits
    public void visitUnderline(Underline underline) {
        final XmlUnderlineType xmlUnderline = factory.createXmlUnderlineType();
        final XmlBeginEndType beginEndType = switch (underline.getBeginEnd()) {
            case BEGIN -> XmlBeginEndType.BEGIN;
            case END -> XmlBeginEndType.END;
        };
        xmlUnderline.setType(beginEndType);
        wordElements.add(factory.createUnderline(xmlUnderline));
    }

    @Override
    @Visits
    public void visitItalic(Italic italic) {
        final XmlItalicType xmlItalic = factory.createXmlItalicType();
        final XmlBeginEndType beginEndType = switch (italic.getBeginEnd()) {
            case BEGIN -> XmlBeginEndType.BEGIN;
            case END -> XmlBeginEndType.END;
        };
        xmlItalic.setType(beginEndType);
        wordElements.add(factory.createItalic(xmlItalic));
    }

    @Override
    public void fallbackVisit(WordElement obj) {
    }

}
