package ca.phon.orthography;

public final class Replacement extends AbstractOrthographyElement {

    public static final String PREFIX_REAL = "[::";

    public static final String PREFIX = "[:";

    private final boolean real;

    private final String data;

    public Replacement(String data) {
        this(false, data);
    }

    public Replacement(boolean real, String data) {
        super();
        this.real = real;
        this.data = data;
    }

    public boolean isReal() {
        return real;
    }

    public String getData() {
        return data;
    }

    @Override
    public String text() {
        final String prefix = isReal() ? PREFIX_REAL : PREFIX;
        return String.format("%s %s]", prefix, getData());
    }

}
