package ca.phon.orthography.mor;

import ca.phon.util.Documentation;

/**
 * Mor marker
 */
@Documentation("https://talkbank.org/manuals/MOR.html#Mor_Markers")
public final class MorMarker {

    private final MorMarkerType type;

    private final String text;

    public MorMarker(MorMarkerType type, String text) {
        super();
        this.type = type;
        this.text = text;
    }

    public MorMarkerType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return getType().getPrefix() + getText();
    }

}
