package ca.phon.csv;

/**
 * The type of quote used by a given CSV file.
 * @author Cristopher Yates
 * */
public enum CSVQuoteType {
    DOUBLE_QUOTE('"'),
    SINGLE_QUOTE('\'');

    private final char quoteChar;

    /**
     * Constructs a new CSVQuoteType.
     * */
    CSVQuoteType(char c) {
        this.quoteChar = c;
    }

    /**
     * Gets the character associated with the quote type.
     * */
    public char getQuoteChar() {
        return this.quoteChar;
    }
}
