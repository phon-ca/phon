package ca.phon.orthography;

/**
 * Prosody inside word
 */
public final class Prosody extends AbstractOrthoWordElement {

    private ProsodyType type;

    public Prosody(ProsodyType type) {
        super();
        this.type = type;
    }

    public ProsodyType getType() {
        return this.type;
    }

    @Override
    public String getText() {
        return this.type.toString();
    }

}
