package ca.phon.orthography;

public final class Terminator extends AbstractOrthographyElement {

    private final TerminatorType type;

    public Terminator(TerminatorType type) {
        super();
        this.type = type;
    }

    public TerminatorType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}
