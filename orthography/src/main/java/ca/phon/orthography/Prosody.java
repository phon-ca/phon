package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Prosody inside word
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#Prosody")
public final class Prosody extends AbstractWordElement {

    private ProsodyType type;

    public Prosody(ProsodyType type) {
        super();
        this.type = type;
    }

    public ProsodyType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return this.type.toString();
    }

}
