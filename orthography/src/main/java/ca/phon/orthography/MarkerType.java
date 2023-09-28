package ca.phon.orthography;

import ca.phon.util.Documentation;

public enum MarkerType {
    @Documentation("https://talkbank.org/manuals/CHAT.html#Stressing_Scope")
    STRESSING("[!]", "stressing"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#ContrastiveStressing_Scope")
    CONTRASTIVE_STRESSING("[!!]", "contrastive stressing"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#BestGuess_Scope")
    BEST_GUESS("[?]", "best guess"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Repetition_Scope")
    RETRACING("[/]", "retracing"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Retracing_Scope")
    RETRACING_WITH_CORRECTION("[//]", "retracing with correction"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Reformulation_Scope")
    RETRACING_REFORMULATION("[///]", "retracing reformulation"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#UnclearRetracing_Scope")
    RETRACING_UNCLEAR("[/?]", "retracing unclear"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#FalseStart_Scope")
    FALSE_START("[/-]", "false start"),
    /**
     * Content to be excluded, mor exclude in CLAN
     * If applied to the utterance, record.isExcludeFromSearhes() will return true
     */
    @Documentation("https://talkbank.org/manuals/CHAT.html#MorExclude_Scope")
    EXCLUDE("[e]", "exclude");

    private String text;

    private String displayString;

    private MarkerType(String text, String displayString) {
        this.text = text;
        this.displayString = displayString;
    }

    public String getText() {
        return text;
    }

    public String getDisplayString() {
        return displayString;
    }

    @Override
    public String toString() {
        return getText();
    }

    public static MarkerType fromString(String text) {
        for(MarkerType type:values()) {
            if(type.getText().equals(text) || type.getDisplayString().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
