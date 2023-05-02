package ca.phon.orthography;

public final class Postcode extends AbstractOrthographyElement {

    public final static String POSTCODE_PREFIX = "[+";

    private final String code;

    public Postcode(String code) {
        super();
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String text() {
        return String.format("%s %s]", POSTCODE_PREFIX, getCode());
    }

}
