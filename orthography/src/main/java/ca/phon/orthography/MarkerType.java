package ca.phon.orthography;

public enum MarkerType {
    STRESSING("[!]", "stressing"),
    CONTRASTIVE_STRESSING("[!!]", "contrastive stressing"),
    BEST_GUESS("[?]", "best guess"),
    RETRACING("[/]", "retracing"),
    RETRACING_WITH_CORRECTION("[//]", "retracing with correction"),
    RETRACING_REFORMULATION("[///]", "retracing reformulation"),
    RETRACING_UNCLEAR("[/?]", "retracing unclear"),
    FALSE_START("[/-]", "false start"),
    MOR_EXCLUDE("[e]", "mor exclude");

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
            if(type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }

}
