package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.xml.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class XmlWordContentVisitor extends VisitorAdapter<Object> {

    private Langs langs = new Langs();

    private final List<WordPos> wordPos = new ArrayList<>();

    private final List<Replacement> replacements = new ArrayList<>();

    private List<WordElement> wordElements = new ArrayList<>();

    private boolean isCompound = false;

    private final Stack<List<WordElement>> compoundWordStack = new Stack<>();

    private final Stack<CompoundWordMarkerType> compoundWordMarkerTypes = new Stack<>();

    @Visits
    public void visitLangs(XMLOrthographyLangs xmlLangs) {
        if(xmlLangs.getSingle() != null) {
            langs = new Langs(Langs.LangsType.SINGLE, xmlLangs.getSingle());
        } else if(xmlLangs.getMultiple() != null) {
            langs = new Langs(Langs.LangsType.MULTIPLE, xmlLangs.getMultiple().toArray(new String[0]));
        } else if(xmlLangs.getAmbiguous() != null) {
            langs = new Langs(Langs.LangsType.AMBIGUOUS, xmlLangs.getAmbiguous().toArray(new String[0]));
        }
    }

    @Visits
    public void visitCaDelimiter(XMLOrthographyCaDelimiter xmlCaDelim) {
        final BeginEnd beginEnd =
                (xmlCaDelim.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        final CaDelimiterType caDelimiterType = switch(xmlCaDelim.getLabel()) {
            case BREATHY_VOICE ->  CaDelimiterType.BREATHY_VOICE;
            case CREAKY -> CaDelimiterType.CREAKY;
            case FASTER -> CaDelimiterType.FASTER;
            case HIGH_PITCH -> CaDelimiterType.HIGH_PITCH;
            case LOUDER -> CaDelimiterType.LOUDER;
            case LOW_PITCH -> CaDelimiterType.LOW_PITCH;
            case PRECISE -> CaDelimiterType.PRECISE;
            case REPEATED_SEGMENT -> CaDelimiterType.REPEATED_SEGMENT;
            case SINGING -> CaDelimiterType.SINGING;
            case SLOWER -> CaDelimiterType.SLOWER;
            case SMILE_VOICE -> CaDelimiterType.SMILE_VOICE;
            case SOFTER -> CaDelimiterType.SOFTER;
            case UNSURE -> CaDelimiterType.UNSURE;
            case WHISPER -> CaDelimiterType.WHISPER;
            case YAWN -> CaDelimiterType.YAWN;
        };
        wordElements.add(new CaDelimiter(beginEnd, caDelimiterType));
    }

    @Visits
    public void visitCaElement(XMLOrthographyCaElement xmlCaEle) {
        final CaElementType caElementType = switch(xmlCaEle.getType()) {
            case BLOCKED_SEGMENTS -> CaElementType.BLOCKED_SEGMENTS;
            case CONSTRICTION -> CaElementType.CONSTRICTION;
            case INHALATION -> CaElementType.INHALATION;
            case LAUGH_IN_WORD -> CaElementType.LAUGH_IN_WORD;
            case PITCH_DOWN -> CaElementType.PITCH_DOWN;
            case PITCH_RESET -> CaElementType.PITCH_RESET;
            case PITCH_UP -> CaElementType.PITCH_UP;
            case PRIMARY_STRESS -> CaElementType.PRIMARY_STRESS;
            case SECONDARY_STRESS -> CaElementType.SECONDARY_STRESS;
        };
        wordElements.add(new CaElement(caElementType));
    }

    @Visits
    public void visitLongFeature(XMLOrthographyLongFeature xmlLongFeature) {
        final BeginEnd beginEnd =
                (xmlLongFeature.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new LongFeature(beginEnd, xmlLongFeature.getValue()));
    }

    @Visits
    public void visitOverlapPoint(XMLOrthographyOverlapPoint xmlOverlapPt) {
        final OverlapPointType type = OverlapPointType.fromDescription(xmlOverlapPt.getTopBottom(), xmlOverlapPt.getStartEnd());
        final int index = xmlOverlapPt.getIndex() != null ? xmlOverlapPt.getIndex().intValue() : -1;
        wordElements.add(new OverlapPoint(type, index));
    }

    @Visits
    public void visitProsody(XMLOrthographyP xmlP) {
        final ProsodyType type = ProsodyType.fromString(xmlP.getType());
        wordElements.add(new Prosody(type));
    }

    @Visits
    public void visitShortening(XMLOrthographyShortening xmlShortening) {
        wordElements.add(new Shortening(xmlShortening.getValue()));
    }

    @Visits
    public void visitWk(XMLOrthographyWk xmlWk) {
        final CompoundWordMarkerType type = switch(xmlWk.getType()) {
            case "cmp" -> CompoundWordMarkerType.COMPOUND;
            case "cli" -> CompoundWordMarkerType.CLITIC;
            default -> throw new IllegalArgumentException(xmlWk.getType());
        };
        if(wordElements.size() == 0)
            throw new IllegalStateException("invalid compound word");
        isCompound = true;
        compoundWordStack.push(wordElements);
        compoundWordMarkerTypes.push(type);
        wordElements = new ArrayList<>();
    }

    @Visits
    public void visitUnderline(XMLOrthographyUnderline xmlUnderline) {
        final BeginEnd beginEnd =
                (xmlUnderline.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new Underline(beginEnd));
    }

    @Visits
    public void visitItalic(XMLOrthographyItalic xmlItalic) {
        final BeginEnd beginEnd =
                (xmlItalic.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new Italic(beginEnd));
    }

    @Visits
    public void visitPos(XMLOrthographyPos xmlPos) {
        wordPos.add(new WordPos(xmlPos.getC(), xmlPos.getS()));
    }

    @Visits
    public void visitReplacement(XMLOrthographyReplacement xmlReplacement) {
        final XmlOrthographyVisitor orthoVisitor = new XmlOrthographyVisitor();
        xmlReplacement.getW().forEach(orthoVisitor::visitWord);
        final Orthography ortho = orthoVisitor.getOrthography();
        final List<Word> wordList = new ArrayList<>();
        for(OrthographyElement ele:ortho) {
            if(!(ele instanceof Word)) throw new IllegalArgumentException();
            wordList.add((Word)ele);
        }
        replacements.add(new Replacement(xmlReplacement.isReal(), wordList));
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalArgumentException(obj.getClass().getName());
    }

    public Langs getLangs() {
        return langs;
    }

    public List<WordPos> getWordPos() {
        return wordPos;
    }

    public List<Replacement> getReplacements() {
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
