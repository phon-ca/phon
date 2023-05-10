package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.WordType;
import ca.phon.orthography.xml.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.ArrayList;
import java.util.List;

public class XmlOrthographyVisitor extends VisitorAdapter<Object> {

    private OrthographyBuilder builder = new OrthographyBuilder();

    @Visits
    public void visitWord(XMLOrthographyW word) {
        final WordType wordType = word.getType() == null ? null : switch (word.getType()) {
            case FILLER -> WordType.FILLER;
            case FRAGMENT -> WordType.FRAGMENT;
            case NONWORD -> WordType.NONWORD;
            case OMISSION -> WordType.OMISSION;
        };
        final WordPrefix prefix = wordType != null ? new WordPrefix(wordType) : null;
        final String userSpecialForm = word.getUserSpecialForm();
        final WordFormType formType = word.getFormType() == null ? null : switch (word.getFormType()) {
            case ADDITION -> WordFormType.ADDITION;
            case BABBLING -> WordFormType.BABBLING;
            case CHILD_INVENTED -> WordFormType.CHILD_INVENTED;
            case DIALECT -> WordFormType.DIALECT;
            case ECHOLALIA -> WordFormType.ECHOLALIA;
            case FAMILY_SPECIFIC -> WordFormType.FAMILY_SPECIFIC;
            case FILLED_PAUSE -> WordFormType.FILLED_PAUSE;
            case GENERIC -> WordFormType.GENERIC;
            case INTERJECTION -> WordFormType.INTERJECTION;
            case KANA -> WordFormType.KANA;
            case LETTER -> WordFormType.LETTER;
            case NEOLOGISM -> WordFormType.NEOLOGISM;
            case NO_VOICE -> WordFormType.NO_VOICE;
            case ONOMATOPOEIA -> WordFormType.ONOMATOPOEIA;
            case PHONOLOGY_CONSISTENT -> WordFormType.PHONOLOGY_CONSISTENT;
            case QUOTED_METAREFERENCE -> WordFormType.QUOTED_METAREFERENCE;
            case SIGNED_LANGUAGE -> WordFormType.SIGNED_LANGUAGE;
            case SIGN_SPEECH -> WordFormType.SIGN_SPEECH;
            case SINGING -> WordFormType.SINGING;
            case TEST -> WordFormType.TEST;
            case UNIBET -> WordFormType.UNIBET;
            case WORDS_TO_BE_EXCLUDED -> WordFormType.WORDS_TO_BE_EXCLUDED;
            case WORD_PLAY -> WordFormType.WORD_PLAY;
        };
        final UntranscribedType untranscribedType = word.getUntranscribed() == null ? null : switch (word.getUntranscribed()) {
            case UNTRANSCRIBED -> UntranscribedType.UNTRANSCRIBED;
            case UNINTELLIGIBLE -> UntranscribedType.UNINTELLIGIBLE;
            case UNINTELLIGIBLE_WITH_PHO -> UntranscribedType.UNINTELLIGIBLE_WORD_WITH_PHO;
        };
        final List<WordPos> wordPos = new ArrayList<>();

        final WordSuffix suffix = new WordSuffix(word.isSeparatedPrefix(), formType, word.getFormSuffix(), userSpecialForm, wordPos);
        final XmlWordContentVisitor visitor = new XmlWordContentVisitor();
        word.getContent().forEach(visitor::visit);
        if(visitor.isCompound()) {
            List<WordElement> w1Eles = visitor.getCompoundWordStack().pop();
            List<WordElement> w2Eles = visitor.getWordElements();
            CompoundWordMarkerType type = visitor.getCompoundWordMarkerTypes().pop();
            CompoundWord compoundWord = new CompoundWord(new Word(w1Eles.toArray(new WordElement[0])), new Word(w2Eles.toArray(new WordElement[0])), new CompoundWordMarker(type));
            while(!visitor.getCompoundWordStack().isEmpty()) {
                w1Eles = visitor.getCompoundWordStack().pop();
                type = visitor.getCompoundWordMarkerTypes().pop();
                compoundWord = new CompoundWord(new Word(w1Eles.toArray(new WordElement[0])), compoundWord, type);
            }
            compoundWord = new CompoundWord(visitor.getLangs(), prefix, suffix,
                    compoundWord.getWord1(), compoundWord.getWord2(), compoundWord.getMarker());
            builder.append(compoundWord);
        } else {
            final Word w = new Word(visitor.getLangs(), visitor.getReplacements(),
                    prefix, suffix, untranscribedType, visitor.getWordElements().toArray(new WordElement[0]));
            builder.append(w);
        }
    }

    @Visits
    public void visitLinker(XMLOrthographyLinker xmlLinker) {
        final LinkerType type = switch (xmlLinker.getType()) {
            case LAZY_OVERLAP_MARK -> LinkerType.LAZY_OVERLAP_MARK;
            case NO_BREAK_TCU_COMPLETION -> LinkerType.NO_BREAK_TCU_COMPLETION;
            case OTHER_COMPLETION -> LinkerType.OTHER_COMPLETION;
            case QUICK_UPTAKE -> LinkerType.QUICK_UPTAKE;
            case QUOTED_UTTERANCE_NEXT -> LinkerType.QUOTED_UTTERANCE_NEXT;
            case SELF_COMPLETION -> LinkerType.SELF_COMPLETION;
            case TECHNICAL_BREAK_TCU_COMPLETION -> LinkerType.TECHNICAL_BREAK_TCU_COMPLETION;
        };
        builder.append(new Linker(type));
    }

    @Visits
    public void visitGroup(XMLOrthographyG xmlGroup) {
        final XmlOrthographyVisitor innerVisitor = new XmlOrthographyVisitor();
        xmlGroup.getWOrGOrPg().forEach(innerVisitor::visit);
        final XmlOrthographyAnnotationVisitor annotationVisitor = new XmlOrthographyAnnotationVisitor();
        xmlGroup.getKOrErrorOrDuration().forEach(annotationVisitor::visit);
        builder.append(new OrthoGroup(innerVisitor.getOrthography().toList(), annotationVisitor.getAnnotations()));
    }

    @Visits
    public void visitPhoneticGroup(XMLOrthographyPg xmlPg) {
        final XmlOrthographyVisitor innerVisitor = new XmlOrthographyVisitor();
        xmlPg.getWOrGOrE().forEach(innerVisitor::visit);
        builder.append(new PhoneticGroup(innerVisitor.getOrthography().toList()));
    }

    @Visits
    public void visitQuotation(XMLOrthographyQuotation xmlQuotation) {
        final BeginEnd beginEnd =
                (xmlQuotation.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        builder.append(new Quotation(beginEnd));
    }

    @Visits
    public void visitPause(XMLOrthographyPause xmlPause) {
        if(xmlPause.getSymbolicLength() != null) {
            final PauseLength length = switch (xmlPause.getSymbolicLength()) {
                case SIMPLE -> PauseLength.SIMPLE;
                case LONG -> PauseLength.LONG;
                case VERY_LONG -> PauseLength.VERY_LONG;
            };
            builder.append(new Pause(length));
        } else if(xmlPause.getLength() != null) {
            builder.append(new Pause(PauseLength.NUMERIC, xmlPause.getLength().floatValue() / 1000.0f));
        }
    }

    @Visits
    public void visitInternalMedia(XMLOrthographyMediaType xmlMedia) {
        float start = xmlMedia.getStart().floatValue();
        if(xmlMedia.getUnit() == XMLOrthographyMediaUnitType.MS) {
            start /= 1000.0f;
        }
        float end = xmlMedia.getEnd().floatValue();
        if(xmlMedia.getUnit() == XMLOrthographyMediaUnitType.MS) {
            end /= 1000.0f;
        }
        builder.append(new InternalMedia(start, end));
    }

    @Visits
    public void visitFreecode(XMLOrthographyFreecode xmlFreecode) {
        builder.append(new Freecode(xmlFreecode.getValue()));
    }

    @Visits
    public void visitEvent(XMLOrthographyE xmlEvent) {
        final XmlOrthographyAnnotationVisitor annotationVisitor = new XmlOrthographyAnnotationVisitor();
        xmlEvent.getKOrErrorOrOverlap().forEach(annotationVisitor::visit);
        if(xmlEvent.getAction() != null) {
            builder.append(new Action(annotationVisitor.getAnnotations()));
        } else if(xmlEvent.getHappening() != null) {
            builder.append(new Happening(xmlEvent.getHappening(), annotationVisitor.getAnnotations()));
        } else if(xmlEvent.getOtherSpokenEvent() != null) {
            builder.append(new OtherSpokenEvent(xmlEvent.getOtherSpokenEvent().getWho(), xmlEvent.getOtherSpokenEvent().getSaid(), annotationVisitor.getAnnotations()));
        }
    }

    @Visits
    public void visitSeparator(XMLOrthographyS xmlSeparator) {
        final SeparatorType sType = switch (xmlSeparator.getType()) {
            case CLAUSE_DELIMITER -> SeparatorType.CLAUSE_DELIMITER;
            case COLON -> SeparatorType.COLON;
            case SEMICOLON -> SeparatorType.SEMICOLON;
            case UNMARKED_ENDING -> SeparatorType.UNMARKED_ENDING;
            case UPTAKE -> SeparatorType.UPTAKE;
        };
        builder.append(new Separator(sType));

    }

    @Visits
    public void visitToneMarker(XMLOrthographyToneMarker xmlToneMarker) {
        final ToneMarkerType tType = switch (xmlToneMarker.getType()) {
            case FALLING_TO_LOW -> ToneMarkerType.FALLING_TO_LOW;
            case FALLING_TO_MID -> ToneMarkerType.FALLING_TO_MID;
            case LEVEL -> ToneMarkerType.LEVEL;
            case RISING_TO_HIGH -> ToneMarkerType.RISING_TO_HIGH;
            case RISING_TO_MID -> ToneMarkerType.RISING_TO_MID;
        };
        builder.append(new ToneMarker(tType));
    }

    @Visits
    public void visitTagMarker(XMLOrthographyTagMarker xmlTagMarker) {
        final TagMarkerType type = switch (xmlTagMarker.getType()) {
            case COMMA -> TagMarkerType.COMMA;
            case TAG -> TagMarkerType.TAG;
            case VOCATIVE -> TagMarkerType.VOCATIVE;
        };
        builder.append(new TagMarker(type));
    }

    @Visits
    public void visitOverlapPoint(XMLOrthographyOverlapPoint xmlOverlapPt) {
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
        builder.append(new OverlapPoint(type, index));
    }

    @Visits
    public void visitLongFeature(XMLOrthographyLongFeature xmlLongFeature) {
        final BeginEnd beginEnd =
                (xmlLongFeature.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        builder.append(new LongFeature(beginEnd, xmlLongFeature.getValue()));
    }

    @Visits
    public void visitNonvocal(XMLOrthographyNonvocal xmlNonvocal) {
        final BeginEndSimple beginEnd = switch (xmlNonvocal.getType()) {
            case SIMPLE -> BeginEndSimple.SIMPLE;
            case END -> BeginEndSimple.END;
            case BEGIN -> BeginEndSimple.BEGIN;
        };
        builder.append(new Nonvocal(beginEnd, xmlNonvocal.getValue()));
    }

    @Visits
    public void visitUnderline(XMLOrthographyUnderline xmlUnderline) {
        final BeginEnd beginEnd =
                (xmlUnderline.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        builder.append(new Underline(beginEnd));
    }

    @Visits
    public void visitItalic(XMLOrthographyItalic xmlItalic) {
        final BeginEnd beginEnd =
                (xmlItalic.getType() == XMLOrthographyBeginEndType.BEGIN ? BeginEnd.BEGIN : BeginEnd.END);
        builder.append(new Italic(beginEnd));
    }

    @Visits
    public void visitTerminator(XMLOrthographyT xmlTerminator) {
        final TerminatorType type = switch (xmlTerminator.getType()) {
            case BROKEN_FOR_CODING -> TerminatorType.BROKEN_FOR_CODING;
            case E -> TerminatorType.EXCLAMATION;
            case INTERRUPTION -> TerminatorType.INTERRUPTION;
            case INTERRUPTION_QUESTION -> TerminatorType.INTERRUPTION_QUESTION;
            case MISSING_CA_TERMINATOR -> null;
            case NO_BREAK_TCU_CONTINUATION -> TerminatorType.NO_BREAK_TCU_CONTINUATION;
            case P -> TerminatorType.PERIOD;
            case Q -> TerminatorType.QUESTION;
            case QUESTION_EXCLAMATION -> TerminatorType.QUESTION_EXCLAMATION;
            case QUOTATION_NEXT_LINE -> TerminatorType.QUOTATION_NEXT_LINE;
            case QUOTATION_PRECEDES -> TerminatorType.QUOTATION_PRECEDES;
            case SELF_INTERRUPTION -> TerminatorType.SELF_INTERRUPTION;
            case SELF_INTERRUPTION_QUESTION -> TerminatorType.SELF_INTERRUPTION_QUESTION;
            case TECHNICAL_BREAK_TCU_CONTINUATION -> TerminatorType.TECHNICAL_BREAK_TCU_CONTINUATION;
            case TRAIL_OFF -> TerminatorType.TRAIL_OFF;
            case TRAIL_OFF_QUESTION -> TerminatorType.TRAIL_OFF_QUESTION;
        };
        if(type != null)
            builder.append(new Terminator(type));
    }

    @Visits
    public void visitPostcode(XMLOrthographyPostcode xmlPostcode) {
        builder.append(new Postcode(xmlPostcode.getValue()));
    }

    public Orthography getOrthography() {
        return builder.toOrthography();
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalArgumentException(obj.getClass().getName());
    }

}
