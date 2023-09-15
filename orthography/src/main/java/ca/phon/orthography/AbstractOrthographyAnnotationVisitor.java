package ca.phon.orthography;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visit annotations on an {@link AnnotatedOrthographyElement}
 */
public abstract class AbstractOrthographyAnnotationVisitor extends VisitorAdapter<OrthographyAnnotation> {

    @Visits
    public void visitDuration(Duration duration) {}

    @Visits
    public void visitError(Error error) {}

    @Visits
    public void visitMarker(Marker marker) {}

    @Visits
    public void visitGroupAnnotation(GroupAnnotation groupAnnotation) {}

    @Visits
    public void visitOverlap(Overlap overlap) {}

}
