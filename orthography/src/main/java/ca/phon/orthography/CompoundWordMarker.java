package ca.phon.orthography;

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
