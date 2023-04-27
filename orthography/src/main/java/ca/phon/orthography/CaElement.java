package ca.phon.orthography;

public final class CaElement extends AbstractOrthoWordElement {

    private final CaElementType type;

    public CaElement(CaElementType type) {
        super();
        this.type = type;
    }

    public CaElementType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}
