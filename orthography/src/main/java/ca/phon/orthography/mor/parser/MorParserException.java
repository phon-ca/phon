package ca.phon.orthography.mor.parser;

/**
 * Wrapper for all Mor parser errors
 */
public class MorParserException extends RuntimeException {

    /**
     * Position in line of error
     */
    private int positionInLine = -1;

    /**
     * Line number (default = 0)
     */
    private int lineNumber = 0;

    public MorParserException() {
        super();
    }

    public MorParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public MorParserException(String message) {
        super(message);
    }

    public MorParserException(String message, int charPositionInLine) {
        super(message);
        this.positionInLine = charPositionInLine;
    }

    public MorParserException(Throwable cause) {
        super(cause);
    }

    public int getPositionInLine() {
        return positionInLine;
    }

    public void setPositionInLine(int positionInLine) {
        this.positionInLine = positionInLine;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}
