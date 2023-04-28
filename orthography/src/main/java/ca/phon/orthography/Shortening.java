package ca.phon.orthography;

public final class Shortening extends AbstractOrthoWordElement {

    private final OrthoWordText orthoText;

    public Shortening(String data) {
        this(new OrthoWordText(data));
    }

    public Shortening(OrthoWordText orthoText) {
        super();
        this.orthoText = orthoText;
    }

    public OrthoWordText getOrthoText() {
        return this.orthoText;
    }

    @Override
    public String text() {
        return "(" + getOrthoText().text() + ")";
    }

}
