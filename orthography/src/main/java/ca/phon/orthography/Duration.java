package ca.phon.orthography;

public final class Duration extends AbstractOrthographyElement implements OrthographyAnnotation {

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
        final MediaTimeFormat timeFormat = new MediaTimeFormat();
        return String.format("[# %s]", timeFormat.format(getDuration()));
    }

}
