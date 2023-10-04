package ca.phon.orthography.mor;

import ca.phon.orthography.TerminatorType;

public final class MorTerminator extends MorElement {

    private final TerminatorType type;

    public MorTerminator(TerminatorType type) {
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
