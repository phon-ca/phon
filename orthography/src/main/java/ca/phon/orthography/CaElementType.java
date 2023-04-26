package ca.phon.orthography;

public enum CaElementType {
    BLOCKED_SEGMENTS('\u2260', "blocked segments"),
    CONSTRICTION('\u223e', "constriction"),
    INHALATION('\u2219', "inhalation"),
    LAUGH_IN_WORD('\u1f29', "laugh in word"),
    PITCH_DOWN('\u2193', "pitch down"),
    PITCH_RESET('\u21bb', "pitch reset"),
    PITCH_UP('\u2191', "pitch up"),
    PRIMARY_STRESS('\u02c8', "primary stress"),
    SECONDARY_STRESS('\u02cc', "secondary stress");

    private char ch;

    private String displayName;

    private CaElementType(char ch, String displayName) {
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

    public static CaElementType fromString(String text) {
        for(CaElementType type:values()) {
            if(type.toString().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
