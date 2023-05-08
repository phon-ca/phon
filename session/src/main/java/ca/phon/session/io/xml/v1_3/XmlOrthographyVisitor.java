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
        final WordType wordType = word.getType() != null ? WordType.fromString(word.getType()) : null;
        final WordPrefix prefix = wordType != null ? new WordPrefix(wordType) : null;
        final String userSpecialForm = word.getUserSpecialForm();
        final WordFormType formType = word.getFormType() != null ? WordFormType.fromCode(word.getFormType()) : null;
        final UntranscribedType untranscribedType = UntranscribedType.fromString(word.getUntranscribed());
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
        final LinkerType type = LinkerType.fromString(xmlLinker.getType());
        if(type == null) throw new IllegalArgumentException(xmlLinker.getType());
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
        final SeparatorType sType = SeparatorType.fromString(xmlSeparator.getType());
        if(sType != null) {
            builder.append(new Separator(sType));
        } else {
            final ToneMarkerType tType = ToneMarkerType.fromString(xmlSeparator.getType());
            if(tType != null) {
                builder.append(new ToneMarker(tType));
            } else {
                throw new IllegalArgumentException(xmlSeparator.getType());
            }
        }
    }

    @Visits
    public void visitTagMarker(XMLOrthographyTagMarker xmlTagMarker) {
        final TagMarkerType type = TagMarkerType.fromString(xmlTagMarker.getType());
        if(type == null) throw new IllegalArgumentException(xmlTagMarker.getType());
        builder.append(new TagMarker(type));
    }

    @Visits
    public void visitOverlapPoint(XMLOrthographyOverlapPoint xmlOverlapPt) {
        final OverlapPointType type = OverlapPointType.fromDescription(xmlOverlapPt.getTopBottom(), xmlOverlapPt.getStartEnd());
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
        final TerminatorType type = TerminatorType.fromString(xmlTerminator.getType());
        if(type == null) throw new IllegalArgumentException(xmlTerminator.getType());
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
