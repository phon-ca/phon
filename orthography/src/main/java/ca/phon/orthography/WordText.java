package ca.phon.orthography;

public final class WordText extends AbstractWordElement {

    private final String text;

    public WordText(String text) {
        super();
        this.text = text;
    }

    @Override
    public String text() {
        return this.text;
    }

}
