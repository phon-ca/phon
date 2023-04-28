package ca.phon.orthography;

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
