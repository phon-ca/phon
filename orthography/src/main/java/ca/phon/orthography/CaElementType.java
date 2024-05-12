package ca.phon.orthography;

import ca.phon.util.Documentation;

public enum CaElementType {
    @Documentation("https://talkbank.org/manuals/CHAT.html#Blocking_Marker")
    BLOCKED_SEGMENTS('\u2260', "blocked segments"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Constriction_Element")
    CONSTRICTION('\u223e', "constriction"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Inhalation_Element")
    INHALATION('\u2219', "inhalation"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#LaughInWord_Element")
    LAUGH_IN_WORD('\u1f29', "laugh in word"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#PitchDown_Element")
    PITCH_DOWN('\u2193', "pitch down"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#PitchReset_Element")
    PITCH_RESET('\u21bb', "pitch reset"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#PitchUp_Element")
    PITCH_UP('\u2191', "pitch up"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#PrimaryStress_Element")
    PRIMARY_STRESS('\u02c8', "primary stress"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#SecondaryStress_Element")
    SECONDARY_STRESS('\u02cc', "secondary stress"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Hardening_Element")
    HARDENING('⁑', "hardening"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#HurriedStart_Element")
    HURRIED_START('⤇', "hurried start"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#SuddenStop_Element")
    HURRIED_END('⤆', "sudden stop");

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
