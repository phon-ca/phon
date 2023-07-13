package ca.phon.formatter;

public enum MediaTimeFormatStyle {
    MINUTES_AND_SECONDS("(?:([0-9]+)\\:)?([0-9]+)\\.([0-9]*)"),
    PADDED_MINUTES_AND_SECONDS("(?:([0-9]{1,3})\\:)?([0-9]{1,2})\\.([0-9]{1,3})"),
    MILLISECONDS("([0-9]+)");

    private String regex;

    private MediaTimeFormatStyle(String regex) {
        this.regex = regex;
    }

    public String getRegex() { return this.regex; }
}
