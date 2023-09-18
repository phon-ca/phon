package ca.phon.orthography;

import ca.phon.util.Documentation;

import java.util.ArrayList;
import java.util.List;

/**
 * happening, such as sneeze
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Local_Event")
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
    public String elementText() {
        return String.format("%s%s", PREFIX, getData());
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new Happening(getData(), annotations);
    }

}
