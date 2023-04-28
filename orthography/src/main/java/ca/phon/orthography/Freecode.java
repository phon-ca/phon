package ca.phon.orthography;

public class Freecode extends AbstractOrthographyElement {

    private final String data;

    public Freecode(String data) {
        super();
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    @Override
    public String text() {
        return "[^" + getData() + "]";
    }

}
