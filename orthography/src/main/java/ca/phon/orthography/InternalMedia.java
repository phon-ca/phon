package ca.phon.orthography;

public class InternalMedia extends AbstractOrthographyElement {

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
        final MediaTimeFormat timeFormat = new MediaTimeFormat();
        return String.format("%c%s-%s%c",
                MEDIA_BULLET,
                timeFormat.sToDisplayString(getStartTime()),
                timeFormat.sToDisplayString(getEndTime()),
                MEDIA_BULLET);
    }

}
