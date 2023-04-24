package ca.phon.orthography;

public enum TerminatorType {
    PERIOD(".", "period"),
    QUESTION("?", "question"),
    EXCLAMATION("!", "exclamation");

    private String text;

    private String displayName;

    private TerminatorType(String text, String displayName) {
        this.text = text;
        this.displayName = displayName;
    }

}
