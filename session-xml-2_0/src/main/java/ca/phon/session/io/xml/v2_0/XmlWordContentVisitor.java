package ca.phon.session.io.xml.v2_0;

import ca.phon.orthography.mor.Pos;
import ca.phon.orthography.*;
import ca.phon.orthography.ProsodyType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import jakarta.xml.bind.JAXBElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class XmlWordContentVisitor extends VisitorAdapter<Object> {

    private Langs langs = new Langs();

    private final List<Pos> pos = new ArrayList<>();

    private final List<Replacement> replacements = new ArrayList<>();

    private List<WordElement> wordElements = new ArrayList<>();

    private boolean isCompound = false;

    private final Stack<List<WordElement>> compoundWordStack = new Stack<>();

    private final Stack<CompoundWordMarkerType> compoundWordMarkerTypes = new Stack<>();

    @Visits
    public void visitLangs(XmlLangsType xmlLangs) {
        if(xmlLangs.getSingle() != null) {
            langs = new Langs(Langs.LangsType.SINGLE, xmlLangs.getSingle());
        } else if(xmlLangs.getMultiple().size() > 0) {
            langs = new Langs(Langs.LangsType.MULTIPLE, xmlLangs.getMultiple().toArray(new String[0]));
        } else if(xmlLangs.getAmbiguous().size() > 0) {
            langs = new Langs(Langs.LangsType.AMBIGUOUS, xmlLangs.getAmbiguous().toArray(new String[0]));
        } else {
            langs = new Langs(Langs.LangsType.SECONDARY);
        }
    }

    @Visits
    public void visitText(String text) {
        if(text.trim().length() > 0)
            wordElements.add(new WordText(text.trim()));
    }

    @Visits
    public void visitCaDelimiter(XmlCaDelimiterType xmlCaDelim) {
        final BeginEnd beginEnd =
                (xmlCaDelim.getType() == XmlBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
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
    public void visitCaElement(XmlCaElementType xmlCaEle) {
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
            case HARDENING -> CaElementType.HARDENING;
            case HURRIED_START -> CaElementType.HURRIED_START;
            case SUDDEN_STOP -> CaElementType.SUDDEN_STOP;
        };
        wordElements.add(new CaElement(caElementType));
    }

    @Visits
    public void visitLongFeature(XmlLongFeatureType xmlLongFeature) {
        final BeginEnd beginEnd =
                (xmlLongFeature.getType() == XmlBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new LongFeature(beginEnd, xmlLongFeature.getContent()));
    }

    @Visits
    public void visitOverlapPoint(XmlOverlapPointType xmlOverlapPt) {
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
        wordElements.add(new OverlapPoint(type, index));
    }

    @Visits
    public void visitProsody(XmlProsodyType xmlP) {
        final ProsodyType type = switch (xmlP.getType()) {
            case DRAWL -> ProsodyType.DRAWL;
            case PAUSE -> ProsodyType.PAUSE;
        };
        wordElements.add(new Prosody(type));
    }

    @Visits
    public void visitShortening(XmlShorteningType xmlShortening) {
        wordElements.add(new Shortening(xmlShortening.getValue()));
    }

    @Visits
    public void visitWk(XmlCompoundType xmlWk) {
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
    public void visitUnderline(XmlUnderlineType xmlUnderline) {
        final BeginEnd beginEnd =
                (xmlUnderline.getType() == XmlBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new Underline(beginEnd));
    }

    @Visits
    public void visitItalic(XmlItalicType xmlItalic) {
        final BeginEnd beginEnd =
                (xmlItalic.getType() == XmlBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        wordElements.add(new Italic(beginEnd));
    }

    @Visits
    public void visitPos(XmlPosType xmlPos) {
        pos.add(new Pos(xmlPos.getC().getValue(),
                xmlPos.getSubc().stream().map(XmlSubcategoryType::getValue).toList()));
    }

    @Visits
    public void visitReplacement(XmlReplacementType xmlReplacement) {
        final XmlOrthographyVisitor orthoVisitor = new XmlOrthographyVisitor();
        xmlReplacement.getW().forEach(orthoVisitor::visitWord);
        final Orthography ortho = orthoVisitor.getOrthography();
        final List<Word> wordList = new ArrayList<>();
        for(OrthographyElement ele:ortho) {
            if(!(ele instanceof Word word)) throw new IllegalArgumentException();
            wordList.add(word);
        }
        replacements.add(new Replacement(xmlReplacement.isReal() != null && xmlReplacement.isReal(), wordList));
    }

    @Override
    public void fallbackVisit(Object obj) {
        if(obj instanceof JAXBElement<?>) {
            final JAXBElement<?> ele = (JAXBElement<?>) obj;
            visit(ele.getValue());
            return;
        }
        throw new IllegalArgumentException(obj.getClass().getName());
    }

    public Langs getLangs() {
        return langs;
    }

    public List<Pos> getWordPos() {
        return pos;
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
