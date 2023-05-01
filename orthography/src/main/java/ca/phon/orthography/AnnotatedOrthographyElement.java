package ca.phon.orthography;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AnnotatedOrthographyElement extends AbstractOrthographyElement {

    private List<OrthographyAnnotation> annotations;

    public AnnotatedOrthographyElement(List<OrthographyAnnotation> annotations) {
        super();
        this.annotations = Collections.unmodifiableList(annotations);
    }

    public List<OrthographyAnnotation> getAnnotations() {
        return this.annotations;
    }

    protected String getAnnotationText() {
        if(getAnnotations().size() > 0) {
            return " " + getAnnotations().stream()
                    .map(annotation -> annotation.text())
                    .collect(Collectors.joining(" "));
        } else
            return "";
    }

    public abstract AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation);

}
