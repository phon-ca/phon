package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

public final class Happening extends Event {

    public final static String PREFIX = "&=";

    private final String data;

    public Happening(String text) {
        this(text, new ArrayList<>());
    }

    public Happening(String data, List<OrthographyAnnotation> annotations) {
        super(annotations);
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    @Override
    public String text() {
        return String.format("%s%s", PREFIX, getData()) + getAnnotationText();
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new Happening(getData(), annotations);
    }

}
