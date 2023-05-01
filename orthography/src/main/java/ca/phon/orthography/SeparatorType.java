package ca.phon.orthography;

public enum SeparatorType {
    SEMICOLON(";", "semicolon"),
    COLON(":", "colon"),
    CLAUSE_DELIMITER("[c]", "clause delimiter");

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
            if(type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
