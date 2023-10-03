package ca.phon.orthography;

import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.util.Documentation;

/**
 * Duration annotation, printed in minutes and seconds.
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Duration_Scope")
public final class Duration extends AbstractOrthographyElement implements OrthographyAnnotation {

    /**
     * Duration in seconds
     */
    private final float duration;

    public Duration(float duration) {
        super();
        this.duration = duration;
    }

    public float getDuration() {
        return duration;
    }

    @Override
    public String text() {
        return String.format("[# %s]", MediaTimeFormatter.timeToMinutesAndSeconds(getDuration()));
    }

}