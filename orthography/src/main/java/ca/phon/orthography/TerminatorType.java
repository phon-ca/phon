package ca.phon.orthography;

public enum TerminatorType {
    PERIOD(".", "period"),
    QUESTION("?", "question"),
    EXCLAMATION("!", "exclamation"),
    BROKEN_FOR_CODING("+.", "broken for coding"),
    TRAIL_OFF("+...", "trail off"),
    TRAIL_OFF_QUESTION("+..?", "trail off question"),
    QUESTION_EXCLAMATION("+!?", "question exclamation"),
    INTERRUPTION("+/.", "interruption"),
    INTERRUPTION_QUESTION("+/?", "interruption question"),
    SELF_INTERRUPTION("+//.", "self interruption"),
    SELF_INTERRUPTION_QUESTION("+//?", "self interruption question"),
    QUOTATION_NEXT_LINE("+\"/.", "quotation next line"),
    QUOTATION_PRECEDES("+\".", "quotation precedes"),
    TECHNICAL_BREAK_TCU_CONTINUATION("≋", "technical break TCU continuation"),
    NO_BREAK_TCU_CONTINUATION("≈", "no break TCU continuation")
    ;

    private String text;

    private String displayName;

    private TerminatorType(String text, String displayName) {
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
        return this.text;
    }

    public static TerminatorType fromString(String text) {
        for(TerminatorType tt:TerminatorType.values()) {
            if(tt.getText().equals(text) || tt.getDisplayName().equals(text)) {
                return tt;
            }
        }
        return null;
    }

}
