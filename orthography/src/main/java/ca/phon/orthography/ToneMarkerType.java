package ca.phon.orthography;

public enum ToneMarkerType {
    RISING_TO_HIGH('\u21d7', "rising to high"),
    RISING_TO_MID('\u2197', "rising to mid"),
    LEVEL('\u2192', "level"),
    FALLING_TO_MID('\u2198', "falling to mid"),
    FALLING_TO_LOW('\u21d8', "falling to low");

    private char ch;

    private String displayName;

    private ToneMarkerType(char ch, String displayName) {
        this.ch = ch;
        this.displayName = displayName;
    }

    public char getChar() {
        return this.ch;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String toString() {
        return this.getChar() + "";
    }

    public static ToneMarkerType fromString(String text) {
        for(ToneMarkerType type:values()) {
            if(type.toString().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
