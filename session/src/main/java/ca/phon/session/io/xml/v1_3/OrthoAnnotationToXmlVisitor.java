package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.xml.*;

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
    public void visitDuration(Duration duration) {
        final long durationMs = (long)(duration.getDuration() * 1000.0f);
        annotations.add(factory.createDuration(BigDecimal.valueOf(durationMs)));
    }

    @Override
    public void visitError(Error error) {
        annotations.add(factory.createError(error.getData()));
    }

    @Override
    public void visitMarker(Marker marker) {
        final XMLOrthographyK xmlMarker = factory.createXMLOrthographyK();
        final XMLOrthographyMarkerType type = switch (marker.getType()) {
            case BEST_GUESS -> XMLOrthographyMarkerType.BEST_GUESS;
            case CONTRASTIVE_STRESSING -> XMLOrthographyMarkerType.CONTRASTIVE_STRESSING;
            case EXCLUDE -> XMLOrthographyMarkerType.MOR_EXCLUDE;
            case FALSE_START -> XMLOrthographyMarkerType.FALSE_START;
            case RETRACING -> XMLOrthographyMarkerType.RETRACING;
            case RETRACING_REFORMULATION -> XMLOrthographyMarkerType.RETRACING_REFORMULATION;
            case RETRACING_UNCLEAR -> XMLOrthographyMarkerType.RETRACING_UNCLEAR;
            case RETRACING_WITH_CORRECTION -> XMLOrthographyMarkerType.RETRACING_WITH_CORRECTION;
            case STRESSING -> XMLOrthographyMarkerType.STRESSING;
        };
        xmlMarker.setType(type);
        annotations.add(xmlMarker);
    }

    @Override
    public void visitGroupAnnotation(GroupAnnotation groupAnnotation) {
        final XMLOrthographyGa xmlGa = factory.createXMLOrthographyGa();
        final XMLOrthographyGroupAnnotationTypeType type = switch (groupAnnotation.getType()) {
            case ALTERNATIVE -> XMLOrthographyGroupAnnotationTypeType.ALTERNATIVE;
            case COMMENTS -> XMLOrthographyGroupAnnotationTypeType.COMMENTS;
            case EXPLANATION -> XMLOrthographyGroupAnnotationTypeType.EXPLANATION;
            case PARALINGUISTICS -> XMLOrthographyGroupAnnotationTypeType.PARALINGUISTICS;
        };
        xmlGa.setType(type);
        xmlGa.setContent(groupAnnotation.getData());
        annotations.add(xmlGa);
    }

    @Override
    public void visitOverlap(Overlap overlap) {
        final XMLOrthographyOverlap xmlOverlap = factory.createXMLOrthographyOverlap();
        final XMLOrthographyOverlapType type = switch (overlap.getType()) {
            case OVERLAP_FOLLOWS -> XMLOrthographyOverlapType.OVERLAP_FOLLOWS;
            case OVERLAP_PRECEEDS -> XMLOrthographyOverlapType.OVERLAP_PRECEDES;
        };
        xmlOverlap.setType(type);
        if(overlap.getIndex() >= 0)
            xmlOverlap.setIndex(BigInteger.valueOf(overlap.getIndex()));
        annotations.add(xmlOverlap);
    }

    @Override
    public void fallbackVisit(OrthographyAnnotation obj) {

    }

}
