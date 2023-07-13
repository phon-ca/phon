package ca.phon.formatter;

public enum PeriodFormatStyle {
    ISO("P([0-9]+)Y([0-9]+)M([0-9]+)D"),
    PHON("([0-9]+);([0-9]+).([0-9]+)");

    private String regex;

    private PeriodFormatStyle(String regex) {
        this.regex = regex;
    }

    public String getRegex() { return this.regex; }

}
