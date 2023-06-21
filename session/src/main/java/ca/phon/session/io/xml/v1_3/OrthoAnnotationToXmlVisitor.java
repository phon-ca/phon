package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.Langs;
import ca.phon.util.Language;
import ca.phon.visitor.annotation.Visits;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OrthoAnnotationToXmlVisitor extends AbstractOrthographyAnnotationVisitor {

    final ObjectFactory factory = new ObjectFactory();

    private List<Object> annotations;

    public OrthoAnnotationToXmlVisitor() {
        this(new ArrayList<>());
    }

    public OrthoAnnotationToXmlVisitor(List<Object> annotations) {
        this.annotations = annotations;
    }

    @Override
    @Visits
    public void visitDuration(Duration duration) {
        final long durationMs = (long)(duration.getDuration() * 1000.0f);
        final XmlDurationType xmlDurationType = factory.createXmlDurationType();
        xmlDurationType.setValue(BigDecimal.valueOf(durationMs));
        annotations.add(xmlDurationType);
    }

    @Override
    @Visits
    public void visitError(Error error) {
        final XmlErrorType xmlErrorType = factory.createXmlErrorType();
        xmlErrorType.setValue(error.getData());
        annotations.add(xmlErrorType);
    }

    @Override
    @Visits
    public void visitMarker(Marker marker) {
        final XmlMarkerType xmlMarker = factory.createXmlMarkerType();
        final XmlMarkerTypeType type = switch (marker.getType()) {
            case BEST_GUESS -> XmlMarkerTypeType.BEST_GUESS;
            case CONTRASTIVE_STRESSING -> XmlMarkerTypeType.CONTRASTIVE_STRESSING;
            case EXCLUDE -> XmlMarkerTypeType.MOR_EXCLUDE;
            case FALSE_START -> XmlMarkerTypeType.FALSE_START;
            case RETRACING -> XmlMarkerTypeType.RETRACING;
            case RETRACING_REFORMULATION -> XmlMarkerTypeType.RETRACING_REFORMULATION;
            case RETRACING_UNCLEAR -> XmlMarkerTypeType.RETRACING_UNCLEAR;
            case RETRACING_WITH_CORRECTION -> XmlMarkerTypeType.RETRACING_WITH_CORRECTION;
            case STRESSING -> XmlMarkerTypeType.STRESSING;
        };
        xmlMarker.setType(type);
        annotations.add(xmlMarker);
    }

    @Override
    @Visits
    public void visitGroupAnnotation(GroupAnnotation groupAnnotation) {
        final XmlGroupAnnotationType xmlGa = factory.createXmlGroupAnnotationType();
        final XmlGroupAnnotationTypeType type = switch (groupAnnotation.getType()) {
            case ALTERNATIVE -> XmlGroupAnnotationTypeType.ALTERNATIVE;
            case COMMENTS -> XmlGroupAnnotationTypeType.COMMENTS;
            case EXPLANATION -> XmlGroupAnnotationTypeType.EXPLANATION;
            case PARALINGUISTICS -> XmlGroupAnnotationTypeType.PARALINGUISTICS;
        };
        xmlGa.setType(type);
        xmlGa.setContent(groupAnnotation.getData());
        annotations.add(xmlGa);
    }

    @Override
    @Visits
    public void visitOverlap(ca.phon.orthography.Overlap overlap) {
        final XmlOverlapType xmlOverlap = factory.createXmlOverlapType();
        final XmlOverlapTypeType type = switch (overlap.getType()) {
            case OVERLAP_FOLLOWS -> XmlOverlapTypeType.OVERLAP_FOLLOWS;
            case OVERLAP_PRECEDES -> XmlOverlapTypeType.OVERLAP_PRECEDES;
        };
        xmlOverlap.setType(type);
        if(overlap.getIndex() >= 0)
            xmlOverlap.setIndex(BigInteger.valueOf(overlap.getIndex()));
        annotations.add(xmlOverlap);
    }

    @Visits
    @Override
    public void visitLangs(LangsAnnotation langsAnnotation) {
        final Langs langs = langsAnnotation.getLangs();
        final XmlLangsType xmlLangs = factory.createXmlLangsType();
        if(langs.getType() == Langs.LangsType.SINGLE) {
            xmlLangs.setSingle(langs.getLangs().get(0).toString());
        } else if(langs.getType() == Langs.LangsType.MULTIPLE) {
            langs.getLangs().stream().map(Language::toString).forEach(xmlLangs.getMultiple()::add);
        } else if(langs.getType() == Langs.LangsType.AMBIGUOUS) {
            langs.getLangs().stream().map(Language::toString).forEach(xmlLangs.getAmbiguous()::add);
        }
        annotations.add(xmlLangs);
    }

    @Override
    public void fallbackVisit(OrthographyAnnotation obj) {

    }

}
