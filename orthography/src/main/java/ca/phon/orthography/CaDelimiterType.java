package ca.phon.orthography;

import ca.phon.util.Documentation;

public enum CaDelimiterType {
    @Documentation("https://talkbank.org/manuals/CHAT.html#BreathyVoice_Delimiter")
    BREATHY_VOICE('\u264b', "breathy voice"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Creaky_Delimiter")
    CREAKY('\u204e', "creaky"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Faster_Delimiter")
    FASTER('\u2206', "faster"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#HighPitch_Delimiter")
    HIGH_PITCH('\u2594', "high-pitch"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Louder_Delimiter")
    LOUDER('\u25c9', "louder"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#LowPitch_Delimiter")
    LOW_PITCH('\u2581', "low-pitch"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Precise_Delimiter")
    PRECISE('\u00a7', "precise"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#RepeatedSegment_Delimiter")
    REPEATED_SEGMENT('\u21ab', "repeated-segment"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Singing_Delimiter")
    SINGING('\u222e', "singing"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Slower_Delimiter")
    SLOWER('\u2207', "slower"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#SmileVoice_Delimiter")
    SMILE_VOICE('\u263a', "smile voice"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Softer_Delimiter")
    SOFTER('\u00b0', "softer"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Unsure_Delimiter")
    UNSURE('\u2047', "unsure"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Whisper_Delimiter")
    WHISPER('\u222c', "whisper"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Yawn_Delimiter")
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
