package ca.phon.orthography.mor.parser;

/**
 * Wrapper for all Mor parser errors
 */
public class MorParserException extends RuntimeException {

    public enum Type {
        Unknown,
        InvalidToken
    }

    private MorParserException.Type type;

    /**
     * Position in line of error
     */
    private int positionInLine = -1;

    /**
     * Line number (default = 0)
     */
    private int lineNumber = 0;

    public MorParserException() {
        this(MorParserException.Type.Unknown);
    }

    public MorParserException(MorParserException.Type type) {
        super();
        this.type = type;
    }

    public MorParserException(String message, Throwable cause) {
        this(MorParserException.Type.Unknown, message, cause);
    }

    public MorParserException(MorParserException.Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public MorParserException(String message) {
        this(MorParserException.Type.Unknown, message);
    }

    public MorParserException(MorParserException.Type type, String message) {
        this(type, message, -1);
    }

    public MorParserException(String message, int charPositionInLine) {
        this(MorParserException.Type.Unknown, message, charPositionInLine);
    }

    public MorParserException(MorParserException.Type type, String message, int charPositionInLine) {
        super(message);
        this.type = type;
        this.positionInLine = charPositionInLine;
    }

    public MorParserException(MorParserException.Type type, String message, Throwable cause, int charPositionInLine) {
        super(message, cause);
        this.type = type;
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

    public MorParserException.Type getType() {
        return type;
    }

    public void setType(MorParserException.Type type) {
        this.type = type;
    }

}
