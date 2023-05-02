package ca.phon.orthography;

public final class OverlapPoint extends AbstractOrthographyElement implements WordElement {

    private final OverlapPointType type;

    private final int index;

    public OverlapPoint(OverlapPointType type) {
        this(type, -1);
    }

    public OverlapPoint(OverlapPointType type, int index) {
        this.type = type;
        this.index = index;
    }

    public OverlapPointType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String text() {
        return type.getChar() + (getIndex() >= 0 ? "" + getIndex() : "");
    }

}
