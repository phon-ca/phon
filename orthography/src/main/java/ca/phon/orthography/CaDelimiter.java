package ca.phon.orthography;

public final class CaDelimiter extends AbstractWordElement {

    private final CaDelimiterType type;

    public CaDelimiter(CaDelimiterType type) {
        super();
        this.type = type;
    }

    public CaDelimiterType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}