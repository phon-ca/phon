package ca.phon.orthography;

public enum ProsodyType {
    DRAWL(':', "drawl"),
    PAUSE('^', "pause"),
    BLOCKING('^', "blocking"); // at beginning

    private char ch;

    private String displayName;

    private ProsodyType(char ch, String displayName) {
        this.ch = ch;
        this.displayName = displayName;
    }

    public char getChar() {
        return this.ch;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return getChar() + "";
    }

    public static ProsodyType fromString(String string) {
        for(ProsodyType type:values()) {
            if(type.getDisplayName().equals(string)) {
                return type;
            }
        }
        return null;
    }

}
