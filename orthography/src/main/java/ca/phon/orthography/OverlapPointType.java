package ca.phon.orthography;

public enum OverlapPointType {
    TOP_START('⌈', "top-start"),
    TOP_END('⌉', "top-end"),
    BOTTOM_START('⌊', "bottom-start"),
    BOTTOM_END('⌋', "bottom-end");

    private char ch;

    private String displayName;

    private OverlapPointType(char ch, String displayName) {
        this.ch = ch;
        this.displayName = displayName;
    }

    public char getChar() {
        return ch;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getChar() + "";
    }

    public static OverlapPointType fromString(String text) {
        for(OverlapPointType type:values()) {
            if(type.toString().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
