package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Main line terminator
 */
@Documentation({"https://talkbank.org/manuals/CHAT.html#Terminators", "https://talkbank.org/manuals/MOR.html#Terminator_Alignment"})
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
