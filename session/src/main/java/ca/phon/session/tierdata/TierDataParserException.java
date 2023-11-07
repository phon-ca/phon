package ca.phon.session.tierdata;

public class TierDataParserException extends RuntimeException {

    public enum Type {
        Unknown,
        InvalidToken,
        InvalidTimeString,
        MissingOpenBracket,
        MissingCloseBracket,
        MissingMediaBullet
    }

    private TierDataParserException.Type type;

    /**
     * Position in line of error
     */
    private int positionInLine = -1;

    /**
     * Line number (default = 0)
     */
    private int lineNumber = 0;

    public TierDataParserException() {
        this(TierDataParserException.Type.Unknown);
    }

    public TierDataParserException(TierDataParserException.Type type) {
        super();
        this.type = type;
    }

    public TierDataParserException(String message, Throwable cause) {
        this(TierDataParserException.Type.Unknown, message, cause);
    }

    public TierDataParserException(TierDataParserException.Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public TierDataParserException(String message) {
        this(TierDataParserException.Type.Unknown, message);
    }

    public TierDataParserException(TierDataParserException.Type type, String message) {
        this(type, message, -1);
    }

    public TierDataParserException(String message, int charPositionInLine) {
        this(TierDataParserException.Type.Unknown, message, charPositionInLine);
    }

    public TierDataParserException(TierDataParserException.Type type, String message, int charPositionInLine) {
        super(message);
        this.type = type;
        this.positionInLine = charPositionInLine;
    }

    public TierDataParserException(TierDataParserException.Type type, String message, Throwable cause, int charPositionInLine) {
        super(message, cause);
        this.type = type;
        this.positionInLine = charPositionInLine;
    }

    public TierDataParserException(Throwable cause) {
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

    public TierDataParserException.Type getType() {
        return type;
    }

    public void setType(TierDataParserException.Type type) {
        this.type = type;
    }
    
}
