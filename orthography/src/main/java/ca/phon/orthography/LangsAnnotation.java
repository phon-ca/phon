package ca.phon.orthography;

public final class LangsAnnotation extends AbstractOrthographyElement implements OrthographyAnnotation {

    public final static String PREFIX = "[-";

    private final Langs langs;

    public LangsAnnotation(Langs langs) {
        super();
        this.langs = langs;
    }

    public Langs getLangs() {
        return this.langs;
    }

    @Override
    public String text() {
        return String.format("%s %s]", PREFIX, getLangs().toString().substring(3));
    }

}
