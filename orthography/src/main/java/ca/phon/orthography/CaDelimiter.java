package ca.phon.orthography;

public final class CaDelimiter extends AbstractOrthoWordElement {

    private final CaDelimiterType type;

    public CaDelimiter(CaDelimiterType type) {
        super();
        this.type = type;
    }

    public CaDelimiterType getType() {
        return this.type;
    }

    @Override
    public String getText() {
        return type.toString();
    }

}
