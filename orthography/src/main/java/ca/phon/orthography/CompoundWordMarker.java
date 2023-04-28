package ca.phon.orthography;

public final class CompoundWordMarker extends AbstractWordElement {

    private final OrthoCompoundWordMarkerType type;

    public CompoundWordMarker(OrthoCompoundWordMarkerType type) {
        super();
        this.type = type;
    }

    public OrthoCompoundWordMarkerType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.getMarker() + "";
    }

}
