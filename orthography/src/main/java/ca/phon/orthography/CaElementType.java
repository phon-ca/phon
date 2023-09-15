package ca.phon.orthography;

public enum CaElementType {
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Blocking_Marker")
    BLOCKED_SEGMENTS('\u2260', "blocked segments"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Constriction_Element")
    CONSTRICTION('\u223e', "constriction"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Inhalation_Element")
    INHALATION('\u2219', "inhalation"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#LaughInWord_Element")
    LAUGH_IN_WORD('\u1f29', "laugh in word"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#PitchDown_Element")
    PITCH_DOWN('\u2193', "pitch down"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#PitchReset_Element")
    PITCH_RESET('\u21bb', "pitch reset"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#PitchUp_Element")
    PITCH_UP('\u2191', "pitch up"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#PrimaryStress_Element")
    PRIMARY_STRESS('\u02c8', "primary stress"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#SecondaryStress_Element")
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
