package ca.phon.orthography;

public enum CaDelimiterType {
    BREATHY_VOICE('\u264b', "breathy voice"),
    CREAKY('\u204e', "creaky"),
    FASTER('\u2206', "faster"),
    HIGH_PITCH('\u2594', "high-pitch"),
    LOUDER('\u25c9', "louder"),
    LOW_PITCH('\u2581', "low-pitch"),
    PRECISE('\u00a7', "precise"),
    REPEATED_SEGMENT('\u21ab', "repeated-segment"),
    SINGING('\u222e', "singing"),
    SLOWER('\u2207', "slower"),
    SMILE_VOICE('\u26ea', "smile voice"),
    SOFTER('\u00b0', "softer"),
    UNSURE('\u2047', "unsure"),
    WHISPER('\u222c', "whisper"),
    YAWN('\u03ab', "yawn");

    private char ch;

    private String displayName;

    private CaDelimiterType(char ch, String displayName) {
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

    public static CaDelimiterType fromString(String text) {
        for(CaDelimiterType type:values()) {
            if(type.toString().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
