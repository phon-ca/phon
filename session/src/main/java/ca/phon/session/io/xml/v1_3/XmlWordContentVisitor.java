package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.ProsodyType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class XmlWordContentVisitor extends VisitorAdapter<Object> {

    private ca.phon.orthography.Langs langs = new ca.phon.orthography.Langs();

    private final List<WordPos> wordPos = new ArrayList<>();

    private final List<ca.phon.orthography.Replacement> replacements = new ArrayList<>();

    private List<WordElement> wordElements = new ArrayList<>();

    private boolean isCompound = false;

    private final Stack<List<WordElement>> compoundWordStack = new Stack<>();

    private final Stack<CompoundWordMarkerType> compoundWordMarkerTypes = new Stack<>();

    @Visits
    public void visitLangs(ca.phon.session.io.xml.v1_3.Langs xmlLangs) {
        if(xmlLangs.getSingle() != null) {
            langs = new ca.phon.orthography.Langs(ca.phon.orthography.Langs.LangsType.SINGLE, xmlLangs.getSingle());
        } else if(xmlLangs.getMultiple() != null) {
            langs = new ca.phon.orthography.Langs(ca.phon.orthography.Langs.LangsType.MULTIPLE, xmlLangs.getMultiple().toArray(new String[0]));
        } else if(xmlLangs.getAmbiguous() != null) {
            langs = new ca.phon.orthography.Langs(ca.phon.orthography.Langs.LangsType.AMBIGUOUS, xmlLangs.getAmbiguous().toArray(new String[0]));
        }
    }

    @Visits
    public void visitText(String text) {
        wordElements.add(new WordText(text));
    }

    @Visits
    public void visitCaDelimiter(ca.phon.session.io.xml.v1_3.CaDelimiter xmlCaDelim) {
        final BeginEnd beginEnd =
                (xmlCaDelim.getType() == BeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        final ca.phon.orthography.CaDelimiterType caDelimiterType = switch(xmlCaDelim.getLabel()) {
            case BREATHY_VOICE ->  ca.phon.orthography.CaDelimiterType.BREATHY_VOICE;
            case CREAKY -> ca.phon.orthography.CaDelimiterType.CREAKY;
            case FASTER -> ca.phon.orthography.CaDelimiterType.FASTER;
            case HIGH_PITCH -> ca.phon.orthography.CaDelimiterType.HIGH_PITCH;
            case LOUDER -> ca.phon.orthography.CaDelimiterType.LOUDER;
            case LOW_PITCH -> ca.phon.orthography.CaDelimiterType.LOW_PITCH;
            case PRECISE -> ca.phon.orthography.CaDelimiterType.PRECISE;
            case REPEATED_SEGMENT -> ca.phon.orthography.CaDelimiterType.REPEATED_SEGMENT;
            case SINGING -> ca.phon.orthography.CaDelimiterType.SINGING;
            case SLOWER -> ca.phon.orthography.CaDelimiterType.SLOWER;
            case SMILE_VOICE -> ca.phon.orthography.CaDelimiterType.SMILE_VOICE;
            case SOFTER -> ca.phon.orthography.CaDelimiterType.SOFTER;
            case UNSURE -> ca.phon.orthography.CaDelimiterType.UNSURE;
            case WHISPER -> ca.phon.orthography.CaDelimiterType.WHISPER;
            case YAWN -> ca.phon.orthography.CaDelimiterType.YAWN;
        };
        wordElements.add(new ca.phon.orthography.CaDelimiter(beginEnd, caDelimiterType));
    }

    @Visits
    public void visitCaElement(ca.phon.session.io.xml.v1_3.CaElement xmlCaEle) {
        final ca.phon.orthography.CaElementType caElementType = switch(xmlCaEle.getType()) {
            case BLOCKED_SEGMENTS -> ca.phon.orthography.CaElementType.BLOCKED_SEGMENTS;
            case CONSTRICTION -> ca.phon.orthography.CaElementType.CONSTRICTION;
            case INHALATION -> ca.phon.orthography.CaElementType.INHALATION;
            case LAUGH_IN_WORD -> ca.phon.orthography.CaElementType.LAUGH_IN_WORD;
            case PITCH_DOWN -> ca.phon.orthography.CaElementType.PITCH_DOWN;
            case PITCH_RESET -> ca.phon.orthography.CaElementType.PITCH_RESET;
            case PITCH_UP -> ca.phon.orthography.CaElementType.PITCH_UP;
            case PRIMARY_STRESS -> ca.phon.orthography.CaElementType.PRIMARY_STRESS;
            case SECONDARY_STRESS -> ca.phon.orthography.CaElementType.SECONDARY_STRESS;
        };
        wordElements.add(new ca.phon.orthography.CaElement(caElementType));
    }

    @Visits
    public void visitLongFeature(ca.phon.session.io.xml.v1_3.LongFeature xmlLongFeature) {
        final BeginEnd beginEnd =
                (xmlLongFeature.getType() == BeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new ca.phon.orthography.LongFeature(beginEnd, xmlLongFeature.getValue()));
    }

    @Visits
    public void visitOverlapPoint(ca.phon.session.io.xml.v1_3.OverlapPoint xmlOverlapPt) {
        final String topBottom = switch (xmlOverlapPt.getTopBottom()) {
            case TOP -> "top";
            case BOTTOM -> "bottom";
        };
        final String startEnd = switch (xmlOverlapPt.getStartEnd()) {
            case START -> "start";
            case END -> "end";
        };
        final OverlapPointType type = OverlapPointType.fromDescription(topBottom, startEnd);
        final int index = xmlOverlapPt.getIndex() != null ? xmlOverlapPt.getIndex().intValue() : -1;
        wordElements.add(new ca.phon.orthography.OverlapPoint(type, index));
    }

    @Visits
    public void visitProsody(P xmlP) {
        final ca.phon.orthography.ProsodyType type = switch (xmlP.getType()) {
            case BLOCKING -> ca.phon.orthography.ProsodyType.BLOCKING;
            case DRAWL -> ca.phon.orthography.ProsodyType.DRAWL;
            case PAUSE -> ca.phon.orthography.ProsodyType.PAUSE;
        };
        wordElements.add(new Prosody(type));
    }

    @Visits
    public void visitShortening(ca.phon.session.io.xml.v1_3.Shortening xmlShortening) {
        wordElements.add(new ca.phon.orthography.Shortening(xmlShortening.getValue()));
    }

    @Visits
    public void visitWk(Wk xmlWk) {
        final CompoundWordMarkerType type = switch(xmlWk.getType()) {
            case CLI -> CompoundWordMarkerType.CLITIC;
            case CMP -> CompoundWordMarkerType.COMPOUND;
        };
        if(wordElements.size() == 0)
            throw new IllegalStateException("invalid compound word");
        isCompound = true;
        compoundWordStack.push(wordElements);
        compoundWordMarkerTypes.push(type);
        wordElements = new ArrayList<>();
    }

    @Visits
    public void visitUnderline(ca.phon.session.io.xml.v1_3.Underline xmlUnderline) {
        final BeginEnd beginEnd =
                (xmlUnderline.getType() == BeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new ca.phon.orthography.Underline(beginEnd));
    }

    @Visits
    public void visitItalic(ca.phon.session.io.xml.v1_3.Italic xmlItalic) {
        final BeginEnd beginEnd =
                (xmlItalic.getType() == BeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new ca.phon.orthography.Italic(beginEnd));
    }

    @Visits
    public void visitPos(Pos xmlPos) {
        wordPos.add(new WordPos(xmlPos.getC(), xmlPos.getS()));
    }

    @Visits
    public void visitReplacement(ca.phon.session.io.xml.v1_3.Replacement xmlReplacement) {
        final XmlOrthographyVisitor orthoVisitor = new XmlOrthographyVisitor();
        xmlReplacement.getW().forEach(orthoVisitor::visitWord);
        final Orthography ortho = orthoVisitor.getOrthography();
        final List<Word> wordList = new ArrayList<>();
        for(OrthographyElement ele:ortho) {
            if(!(ele instanceof Word)) throw new IllegalArgumentException();
            wordList.add((Word)ele);
        }
        replacements.add(new ca.phon.orthography.Replacement(xmlReplacement.isReal(), wordList));
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalArgumentException(obj.getClass().getName());
    }

    public ca.phon.orthography.Langs getLangs() {
        return langs;
    }

    public List<WordPos> getWordPos() {
        return wordPos;
    }

    public List<ca.phon.orthography.Replacement> getReplacements() {
        return replacements;
    }

    public List<WordElement> getWordElements() {
        return wordElements;
    }

    public boolean isCompound() {
        return isCompound;
    }

    public Stack<List<WordElement>> getCompoundWordStack() {
        return compoundWordStack;
    }

    public Stack<CompoundWordMarkerType> getCompoundWordMarkerTypes() {
        return compoundWordMarkerTypes;
    }

}
