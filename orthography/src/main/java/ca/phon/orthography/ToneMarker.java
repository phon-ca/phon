package ca.phon.orthography;

public final class ToneMarker extends AbstractOrthographyElement {

    private final ToneMarkerType type;

    public ToneMarker(ToneMarkerType type) {
        super();
        this.type = type;
    }

    public ToneMarkerType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}
