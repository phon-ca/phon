package ca.phon.orthography;

public final class OrthoWordText extends AbstractOrthoWordElement {

    private final String text;

    public OrthoWordText(String text) {
        super();
        this.text = text;
    }

    @Override
    public String getText() {
        return this.text;
    }

}
