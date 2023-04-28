package ca.phon.orthography;

public enum PauseLength {
    SIMPLE("(.)", "simple"),
    LONG("(..)", "long"),
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
