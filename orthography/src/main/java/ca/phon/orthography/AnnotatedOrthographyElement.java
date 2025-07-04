package ca.phon.orthography;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class for OrthographyElements with attached annotations
 */
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
            return getAnnotations().stream()
                    .map(annotation -> annotation.text())
                    .collect(Collectors.joining(" "));
        } else
            return "";
    }

    public abstract AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation);

    /**
     * Text for primary element without annotations
     *
     * @return text for primary element without annotations, subclasses should implement this method
     * instead of text()
     */
    public abstract String elementText();

    @Override
    public String text() {
        if(getAnnotations().size() > 0) {
            return String.format("%s %s", elementText(), getAnnotationText());
        } else {
            return elementText();
        }
    }

}
