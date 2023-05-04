package ca.phon.orthography;

public final class CaDelimiter extends AbstractWordElement {

    private final BeginEnd beginEnd;

    private final CaDelimiterType type;

    public CaDelimiter(BeginEnd beginEnd, CaDelimiterType type) {
        super();
        this.beginEnd = beginEnd;
        this.type = type;
    }

    public BeginEnd getBeginEnd() { return this.beginEnd; }

    public CaDelimiterType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}
