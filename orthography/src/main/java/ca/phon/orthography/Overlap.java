package ca.phon.orthography;

public final class Overlap extends AbstractOrthographyElement implements OrthographyAnnotation {

    private OverlapType type;

    private int index;

    public Overlap(OverlapType type) {
        this(type, -1);
    }

    public Overlap(OverlapType type, int index) {
        this.type = type;
        this.index = index;
    }

    public OverlapType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String text() {
        return getType().getPrefix() +
                (getIndex() >=0 ? getIndex() : "") + "]";
    }

}
