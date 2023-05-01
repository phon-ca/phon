package ca.phon.orthography;

public final class Separator extends AbstractOrthographyElement {

    private final SeparatorType type;

    public Separator(SeparatorType type) {
        super();
        this.type = type;
    }

    public SeparatorType getType() {
        return type;
    }

    @Override
    public String text() {
        return getType().getText();
    }

}
