package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.OverlapType;
import ca.phon.session.io.xml.v1_3.Ga;
import ca.phon.session.io.xml.v1_3.K;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class XmlOrthographyAnnotationVisitor extends VisitorAdapter<Object> {

    private final List<OrthographyAnnotation> annotations = new ArrayList<>();

    @Visits
    public void visitMarker(K xmlMarker) {
        final ca.phon.orthography.MarkerType type = switch (xmlMarker.getType()) {
            case BEST_GUESS -> ca.phon.orthography.MarkerType.BEST_GUESS;
            case CONTRASTIVE_STRESSING -> ca.phon.orthography.MarkerType.CONTRASTIVE_STRESSING;
            case FALSE_START -> ca.phon.orthography.MarkerType.FALSE_START;
            case MOR_EXCLUDE -> ca.phon.orthography.MarkerType.EXCLUDE;
            case RETRACING -> ca.phon.orthography.MarkerType.RETRACING;
            case RETRACING_REFORMULATION -> ca.phon.orthography.MarkerType.RETRACING_REFORMULATION;
            case RETRACING_UNCLEAR -> ca.phon.orthography.MarkerType.RETRACING_UNCLEAR;
            case RETRACING_WITH_CORRECTION -> ca.phon.orthography.MarkerType.RETRACING_WITH_CORRECTION;
            case STRESSING -> ca.phon.orthography.MarkerType.STRESSING;
        };
        annotations.add(new Marker(type));
    }

    @Visits
    public void visitError(String error) {
        annotations.add(new Error(error));
    }

    @Visits
    public void visitDuration(BigDecimal duration) {
        annotations.add(new Duration(duration.floatValue() / 1000.0f));
    }

    @Visits
    public void visitGroupAnnotation(Ga xmlGa) {
        final GroupAnnotationType type = switch (xmlGa.getType()) {
            case COMMENTS -> GroupAnnotationType.COMMENTS;
            case ALTERNATIVE -> GroupAnnotationType.ALTERNATIVE;
            case EXPLANATION -> GroupAnnotationType.EXPLANATION;
            case PARALINGUISTICS -> GroupAnnotationType.PARALINGUISTICS;
        };
        annotations.add(new GroupAnnotation(type, xmlGa.getContent()));
    }

    @Visits
    public void visitOverlap(ca.phon.session.io.xml.v1_3.Overlap xmlOverlap) {
        final OverlapType type = switch (xmlOverlap.getType()) {
            case OVERLAP_FOLLOWS -> ca.phon.orthography.OverlapType.OVERLAP_FOLLOWS;
            case OVERLAP_PRECEDES -> ca.phon.orthography.OverlapType.OVERLAP_PRECEDES;
        };
        annotations.add(new ca.phon.orthography.Overlap(type));
    }

    @Visits
    public void visitLangs(ca.phon.session.io.xml.v1_3.Langs langs) {
        // TODO
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalArgumentException(obj.getClass().getName());
    }

    public List<OrthographyAnnotation> getAnnotations() {
        return annotations;
    }

}
