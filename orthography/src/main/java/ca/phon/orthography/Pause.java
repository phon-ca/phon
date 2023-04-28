package ca.phon.orthography;


import ca.phon.util.MsFormatter;

public class Pause extends AbstractOrthoElement {

    private final PauseLength type;

    private final float length;

    public Pause(PauseLength type) {
        this(type, 0.0f);
    }

    public Pause(PauseLength type, float seconds) {
        this.type = type;
        this.length = seconds;
    }

    public float getLength() {
        return this.length;
    }

    public PauseLength getType() {
        return this.type;
    }

    private String lengthToString() {
        return MsFormatter.msToDisplayString((long)(getLength() * 1000.0f));
    }

    @Override
    public String text() {
        return switch (type) {
            case SIMPLE, LONG, VERY_LONG -> type.getText();
            case NUMERIC -> String.format(type.getText(), lengthToString());
        };
    }

}
