package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

public final class Action extends Event {

    public Action() {
        this(new ArrayList<>());
    }

    public Action(List<OrthographyAnnotation> annotations) {
        super(annotations);
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new Action(annotations);
    }

    @Override
    public String text() {
        return "0" + getAnnotationText();
    }

}
