package ca.phon.orthography;

/**
 * Unspoken segment in a word enclosed in parentheses.
 */
@CHATReference({"https://talkbank.org/manuals/CHAT.html#Shortenings","https://talkbank.org/manuals/CHAT.html#Noncompletion_Code"})
public final class Shortening extends AbstractWordElement {

    private final WordText orthoText;

    public Shortening(String data) {
        this(new WordText(data));
    }

    public Shortening(WordText orthoText) {
        super();
        this.orthoText = orthoText;
    }

    public WordText getOrthoText() {
        return this.orthoText;
    }

    @Override
    public String text() {
        return "(" + getOrthoText().text() + ")";
    }

}
