package ca.phon.orthography;

/**
 * Prosody inside word
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#Prosody")
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
