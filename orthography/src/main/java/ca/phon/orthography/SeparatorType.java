package ca.phon.orthography;

public enum SeparatorType {
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Semicolon")
    SEMICOLON(";", "semicolon"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Colon")
    COLON(":", "colon"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#ClauseDelimiter_Scope")
    CLAUSE_DELIMITER("[^c]", "clause delimiter"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#UnmarkedEnding")
    UNMARKED_ENDING("\u221e", "unmarked ending"),
    @CHATReference("https://talkbank.org/manuals/CHAT.html#Uptake")
    UPTAKE("\u2261", "uptake");

    private String text;

    private String displayName;

    private SeparatorType(String text, String displayName) {
        this.text = text;
        this.displayName = displayName;
    }

    public String getText() {
        return text;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getText();
    }

    public static SeparatorType fromString(String text) {
        for(SeparatorType type:values()) {
            if(type.getText().equals(text) || type.getDisplayName().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
