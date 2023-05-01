package ca.phon.orthography;

public final class Marker extends AbstractOrthographyElement implements OrthographyAnnotation {

    private final MarkerType type;

    public Marker(MarkerType type) {
        this.type = type;
    }

    public MarkerType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return getType().getText();
    }

}
