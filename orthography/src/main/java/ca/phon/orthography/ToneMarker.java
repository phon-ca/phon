package ca.phon.orthography;

/**
 * Tone marker
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#ToneDirection_Marker")
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
