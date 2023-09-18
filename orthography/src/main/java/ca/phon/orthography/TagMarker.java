package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Tag marker
 */
@Documentation({"https://talkbank.org/manuals/CHAT.html#Satellite_Marker", "https://talkbank.org/manuals/MOR.html#MorphologicalTagMarker"})
public final class TagMarker extends AbstractOrthographyElement {

    private final TagMarkerType type;

    public TagMarker(TagMarkerType type) {
        super();
        this.type = type;
    }

    public TagMarkerType getType() {
        return type;
    }

    @Override
    public String text() {
        return type.getChar() + "";
    }

}
