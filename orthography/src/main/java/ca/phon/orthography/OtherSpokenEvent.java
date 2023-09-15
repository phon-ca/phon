package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

/**
 * word spoken by someone else during an utterance.
 *
 * Writen as <pre>&*</pre> plus the participant ID
 * e.g., <pre>&*MOT</pre>
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#InterposedWords")
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
    public String elementText() {
        return String.format("%s%s:%s", PREFIX, getWho(), getData());
    }

    @Override
    public AnnotatedOrthographyElement cloneAppendingAnnotation(OrthographyAnnotation annotation) {
        final List<OrthographyAnnotation> annotations = new ArrayList<>(getAnnotations());
        annotations.add(annotation);
        return new OtherSpokenEvent(getWho(), getData(), annotations);
    }

}
