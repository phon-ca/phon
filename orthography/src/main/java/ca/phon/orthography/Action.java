package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action within the transcription.
 *
 * <p>
 * <a href="https://talkbank.org/manuals/CHAT.html#Action_Code">CHAT manual section on this topic</a>
 * </p>
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#Action_Code")
public final class Action extends Event {

    public final static String ACTION_TEXT = "0";

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
    public String elementText() {
        return ACTION_TEXT;
    }

}
