package ca.phon.orthography;

import ca.phon.util.Documentation;

public enum ToneMarkerType {
    @Documentation("https://talkbank.org/manuals/CHAT.html#RisingToHigh")
    RISING_TO_HIGH('\u21d7', "rising to high"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#RisingToMid")
    RISING_TO_MID('\u2197', "rising to mid"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Level")
    LEVEL('\u2192', "level"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#FallingToMid")
    FALLING_TO_MID('\u2198', "falling to mid"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#FallingToLow")
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
            if(type.toString().equals(text) || type.getDisplayName().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
