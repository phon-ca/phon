package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Tone marker
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#ToneDirection_Marker")
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
