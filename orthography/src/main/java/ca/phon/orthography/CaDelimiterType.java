package ca.phon.orthography;

public enum CaDelimiterType {
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#BreathyVoice_Delimiter">CHAT manual section on this topic...</a>
     */
    BREATHY_VOICE('\u264b', "breathy voice"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Creaky_Delimiter">CHAT manual section on this topic...</a>
     */
    CREAKY('\u204e', "creaky"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Faster_Delimiter">CHAT manual section on this topic...</a>
     */
    FASTER('\u2206', "faster"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#HighPitch_Delimiter">CHAT manual section on this topic...</a>
     */
    HIGH_PITCH('\u2594', "high-pitch"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Louder_Delimiter">CHAT manual section on this topic...</a>
     */
    LOUDER('\u25c9', "louder"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#LowPitch_Delimiter">CHAT manual section on this topic...</a>
     */
    LOW_PITCH('\u2581', "low-pitch"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Precise_Delimiter">CHAT manual section on this topic...</a>
     */
    PRECISE('\u00a7', "precise"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#RepeatedSegment_Delimiter">CHAT manual section on this topic...</a>
     */
    REPEATED_SEGMENT('\u21ab', "repeated-segment"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Singing_Delimiter">CHAT manual section on this topic...</a>
     */
    SINGING('\u222e', "singing"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Slower_Delimiter">CHAT manual section on this topic...</a>
     */
    SLOWER('\u2207', "slower"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#SmileVoice_Delimiter">CHAT manual section on this topic...</a>
     */
    SMILE_VOICE('\u263a', "smile voice"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Softer_Delimiter">CHAT manual section on this topic...</a>
     */
    SOFTER('\u00b0', "softer"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Unsure_Delimiter">CHAT manual section on this topic...</a>
     */
    UNSURE('\u2047', "unsure"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Whisper_Delimiter">CHAT manual section on this topic...</a>
     */
    WHISPER('\u222c', "whisper"),
    /**
     * <a href="https://talkbank.org/manuals/CHAT.html#Yawn_Delimiter">CHAT manual section on this topic...</a>
     */
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
