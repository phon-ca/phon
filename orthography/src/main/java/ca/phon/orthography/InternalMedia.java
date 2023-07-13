package ca.phon.orthography;

import ca.phon.formatter.MediaTimeFormatter;

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

    @Override
    public String text() {
        return String.format("%c%s-%s%c",
                MEDIA_BULLET,
                MediaTimeFormatter.timeToMinutesAndSeconds(getStartTime()),
                MediaTimeFormatter.timeToMinutesAndSeconds(getEndTime()),
                MEDIA_BULLET);
    }

}
