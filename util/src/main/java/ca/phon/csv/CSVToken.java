package ca.phon.csv;

/**
 * A tokenized representation of information from a CSV file.
 * @author Cristopher Yates
 * */
public class CSVToken {
    private final CSVTokenType type;
    private final String text;
    // Char position in file
    private final long start;
    // Char pos in line
    private final int charPosInLine;
    // Char length in file
    private final long length;
    private final int lineNumber;

    /**
     * Constructs a new Token
     * @param type The type of the token.
     * @param start The position of the token in the file.
     * @param charPosInLine The position of the token in the current line.
     * @param length The number of characters represented by the token.
     * @param lineNumber The line containing the characters represented by the token.
     * */
    public CSVToken(CSVTokenType type, long start, int charPosInLine, long length, int lineNumber) {
        this.type = type;
        this.text = "";
        this.start = start;
        this.charPosInLine = charPosInLine;
        this.length = length;
        this.lineNumber = lineNumber;
    }

    /**
     * Constructs a new Token
     * @param type The type of the token.
     * @param text The characters represented by the token.
     * @param start The position of the token in the file.
     * @param charPosInLine The position of the token in the current line.
     * @param length The number of characters represented by the token.
     * @param lineNumber The line containing the characters represented by the token.
     * */
    public CSVToken(CSVTokenType type, String text, long start, int charPosInLine, long length, int lineNumber) {
        this.type = type;
        this.text = text;
        this.start = start;
        this.charPosInLine = charPosInLine;
        this.length = length;
        this.lineNumber = lineNumber;
    }

    /**
     * Gets the type of the token.
     * */
    public CSVTokenType getType() {
        return type;
    }

    /**
     * Gets the tokens text.
     * */
    public String getText() {
        return text;
    }
}