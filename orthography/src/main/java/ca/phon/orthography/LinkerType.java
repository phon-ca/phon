package ca.phon.orthography;

public enum LinkerType {
    QUOTED_UTTERANCE_NEXT("+\"", "quoted utterance next"),
    QUICK_UPTAKE("+^", "quick uptake"),
    LAZY_OVERLAP_MARK("+<", "lazy overlap mark"),
    SELF_COMPLETION("+,", "self completion"),
    OTHER_COMPLETION("++", "other completion"),
    TECHNICAL_BREAK_TCU_COMPLETION("+\u224b", "technical completion"),
    NO_BREAK_TCU_COMPLETION("+\u2248", "no break completion");

    private String text;

    private String displayName;

    private LinkerType(String text, String displayName) {
        this.text = text;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return getText();
    }

    public static LinkerType fromString(String text) {
        for(LinkerType lt:values()) {
            if(lt.getText().equals(text)) {
                return lt;
            }
        }
        return null;
    }

}
