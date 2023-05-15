package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

public final class OtherSpokenEvent extends Event {

    public static final String PREFIX = "&*";

    private String who;

    private String data;

    public OtherSpokenEvent(String who, String data) {
        this(who, data, new ArrayList<>());
    }

    public OtherSpokenEvent(String who, String data, List<OrthographyAnnotation> annotations) {
        super(annotations);
        this.who = who;
        this.data = data;
    }

    public String getWho() {
        return who;
    }

    public String getData() {
        return data;
    }

    @Override
    public String text() {
        return String.format("%s%s:%s", PREFIX, getWho(), getData()) + getAnnotationText();
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new OtherSpokenEvent(getWho(), getData(), annotations);
    }

}
