package ca.phon.orthography;

public final class OrthoCompoundWordMarker extends AbstractOrthoWordElement {

    private final OrthoCompoundWordMarkerType type;

    public OrthoCompoundWordMarker(OrthoCompoundWordMarkerType type) {
        super();
        this.type = type;
    }

    public OrthoCompoundWordMarkerType getType() {
        return this.type;
    }

    @Override
    public String getText() {
        return type.getMarker() + "";
    }

}
