package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Unscoped complex local events in the middle of an utterance.
 */
@Documentation("https://talkbank.org/manuals/CHAT.html#ComplexLocalEvents")
public final class Freecode extends AbstractOrthographyElement {

    public final static String PREFIX = "[^";

    private final String code;

    public Freecode(String code) {
        super();
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String text() {
        return String.format("%s %s]", PREFIX, getCode());
    }

}