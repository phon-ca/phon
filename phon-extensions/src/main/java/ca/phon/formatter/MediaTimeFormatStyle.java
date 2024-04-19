package ca.phon.formatter;

public enum MediaTimeFormatStyle {
    MINUTES_AND_SECONDS("Minutes and seconds", "(?:([0-9]+)\\:)?([0-9]+)\\.([0-9]*)"),
    PADDED_MINUTES_AND_SECONDS("Padded minutes and seconds", "(?:([0-9]{1,3})\\:)?([0-9]{1,2})\\.([0-9]{1,3})"),
    MILLISECONDS("Milliseconds", "([0-9]+)");

    private String displayName;

    private String regex;

    private MediaTimeFormatStyle(String displayName, String regex) {
        this.displayName = displayName;
        this.regex = regex;
    }

    public String getRegex() { return this.regex; }

    @Override
    public String toString() {
        return this.displayName;
    }

}
