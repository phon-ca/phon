package ca.phon.session.io.xml.v1_3;

import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.xml.XMLOrthographyGa;
import ca.phon.orthography.xml.XMLOrthographyK;
import ca.phon.orthography.xml.XMLOrthographyOverlap;
import ca.phon.orthography.xml.XMLOrthographyW;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class XmlOrthographyAnnotationVisitor extends VisitorAdapter<Object> {

    private final List<OrthographyAnnotation> annotations = new ArrayList<>();

    @Visits
    public void visitMarker(XMLOrthographyK xmlMarker) {
        final MarkerType type = MarkerType.fromString(xmlMarker.getType());
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
    public void visitGroupAnnotation(XMLOrthographyGa xmlGa) {
        final GroupAnnotationType type = switch (xmlGa.getType()) {
            case COMMENTS -> GroupAnnotationType.COMMENTS;
            case ALTERNATIVE -> GroupAnnotationType.ALTERNATIVE;
            case EXPLANATION -> GroupAnnotationType.EXPLANATION;
            case PARALINGUISTICS -> GroupAnnotationType.PARALINGUISTICS;
        };
        annotations.add(new GroupAnnotation(type, xmlGa.getContent()));
    }

    @Visits
    public void visitOverlap(XMLOrthographyOverlap xmlOverlap) {
        final OverlapType type = OverlapType.fromString(xmlOverlap.getType());
        if(type == null) throw new IllegalArgumentException(xmlOverlap.getType());
        annotations.add(new Overlap(type));
    }

    @Override
    public void fallbackVisit(Object obj) {
        throw new IllegalArgumentException(obj.getClass().getName());
    }

    public List<OrthographyAnnotation> getAnnotations() {
        return annotations;
    }

}
