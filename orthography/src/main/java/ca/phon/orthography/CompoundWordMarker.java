package ca.phon.orthography;

/**
 * Marker between compound words
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#Compounds")
public final class CompoundWordMarker extends AbstractWordElement {

    private final CompoundWordMarkerType type;

    public CompoundWordMarker(CompoundWordMarkerType type) {
        super();
        this.type = type;
    }

    public CompoundWordMarkerType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.getMarker() + "";
    }

}
