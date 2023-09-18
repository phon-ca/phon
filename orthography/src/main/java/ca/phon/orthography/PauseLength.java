package ca.phon.orthography;

import ca.phon.util.Documentation;

public enum PauseLength {
    @Documentation("https://talkbank.org/manuals/CHAT.html#Pause_Default_Length")
    SIMPLE("(.)", "simple"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Pause_Long_Length")
    LONG("(..)", "long"),
    @Documentation("https://talkbank.org/manuals/CHAT.html#Pause_Very_Long_Length")
    VERY_LONG("(...)", "very-long"),
    NUMERIC("(%s)", "numeric");

    private String text;

    private String displayName;

    private PauseLength(String text, String displayName) {
        this.text = text;
        this.displayName = displayName;
    }

    public String getText() {
        return this.text;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.getText();
    }

}
