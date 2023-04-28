package ca.phon.orthography;

public final class Linker extends AbstractOrthographyElement {

    private final LinkerType type;

    public Linker(LinkerType type) {
        super();
        this.type = type;
    }

    public LinkerType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.getText();
    }

}
