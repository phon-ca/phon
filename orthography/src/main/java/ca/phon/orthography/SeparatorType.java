package ca.phon.orthography;

public enum SeparatorType {
    SEMICOLON(";", "semicolon"),
    COLON(":", "colon"),
    CLAUSE_DELIMITER("[c]", "clause delimiter"),
    UNMARKED_ENDING("\u221e", "unmarked ending"),
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
