package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Utterance initiators or linkers; they indicate the way to fit the current
 * utterance with an earlier one.
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Utterance_Linkers")
public final class Linker extends AbstractOrthographyElement {

    private final LinkerType type;

    public Linker(LinkerType type) {
        super();
        this.type = type;
    }

    public LinkerType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.getText();
    }

}