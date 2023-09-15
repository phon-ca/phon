package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.OverlapType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class XmlOrthographyAnnotationVisitor extends VisitorAdapter<Object> {

    private final List<OrthographyAnnotation> annotations = new ArrayList<>();

    @Visits
    public void visitMarker(XmlMarkerType xmlMarker) {
        final MarkerType type = switch (xmlMarker.getType()) {
            case BEST_GUESS -> MarkerType.BEST_GUESS;
            case CONTRASTIVE_STRESSING -> MarkerType.CONTRASTIVE_STRESSING;
            case FALSE_START -> MarkerType.FALSE_START;
            case MOR_EXCLUDE -> MarkerType.EXCLUDE;
            case RETRACING -> MarkerType.RETRACING;
            case RETRACING_REFORMULATION -> MarkerType.RETRACING_REFORMULATION;
            case RETRACING_UNCLEAR -> MarkerType.RETRACING_UNCLEAR;
            case RETRACING_WITH_CORRECTION -> MarkerType.RETRACING_WITH_CORRECTION;
            case STRESSING -> MarkerType.STRESSING;
        };
        annotations.add(new Marker(type));
    }

    @Visits
    public void visitError(XmlErrorType xmlErrorType) {
        annotations.add(new Error(xmlErrorType.getValue()));
    }

    @Visits
    public void visitDuration(BigDecimal duration) {
        annotations.add(new Duration(duration.floatValue() / 1000.0f));
    }

    @Visits
    public void visitGroupAnnotation(XmlGroupAnnotationType xmlGa) {
        final GroupAnnotationType type = switch (xmlGa.getType()) {
            case COMMENTS -> GroupAnnotationType.COMMENTS;
            case ALTERNATIVE -> GroupAnnotationType.ALTERNATIVE;
            case EXPLANATION -> GroupAnnotationType.EXPLANATION;
            case PARALINGUISTICS -> GroupAnnotationType.PARALINGUISTICS;
        };
        annotations.add(new GroupAnnotation(type, xmlGa.getContent()));
    }

    @Visits
    public void visitOverlap(XmlOverlapType xmlOverlap) {
        final OverlapType type = switch (xmlOverlap.getType()) {
            case OVERLAP_FOLLOWS -> ca.phon.orthography.OverlapType.OVERLAP_FOLLOWS;
            case OVERLAP_PRECEDES -> ca.phon.orthography.OverlapType.OVERLAP_PRECEDES;
        };
        final int index = xmlOverlap.getIndex() != null ? xmlOverlap.getIndex().intValue() : -1;
        annotations.add(new ca.phon.orthography.Overlap(type, index));
    }

    @Visits
    public void visitDuration(XmlDurationType xmlDuration) {
        final float value = xmlDuration.getValue().floatValue();
        final Duration duration = new Duration(value / 1000.0f);
        annotations.add(duration);
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalArgumentException(obj.getClass().getName());
    }

    public List<OrthographyAnnotation> getAnnotations() {
        return annotations;
    }

}
