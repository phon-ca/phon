package ca.phon.orthography;

import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.util.Documentation;

/**
 * Media bullet used only before a terminator.
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Internal_Media")
public final class InternalMedia extends AbstractOrthographyElement {

    public final static char MEDIA_BULLET = '•';

    /**
     * Start time in seconds
     */
    private final float startTime;

    /**
     * End time in seconds
     */
    private final float endTime;

    public InternalMedia(float startTime, float endTime) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public float getStartTime() {
        return startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public boolean isUnset() {
        return isPoint() && startTime == 0.0f;
    }

    public boolean isPoint() {
        return startTime == endTime;
    }

    @Override
    public String text() {
        if(isPoint()) {
            return String.format("%c%s%c",
                    MEDIA_BULLET,
                    MediaTimeFormatter.timeToMinutesAndSeconds(getStartTime()),
                    MEDIA_BULLET);
        } else {
            return String.format("%c%s-%s%c",
                    MEDIA_BULLET,
                    MediaTimeFormatter.timeToMinutesAndSeconds(getStartTime()),
                    MediaTimeFormatter.timeToMinutesAndSeconds(getEndTime()),
                    MEDIA_BULLET);
        }
    }

}