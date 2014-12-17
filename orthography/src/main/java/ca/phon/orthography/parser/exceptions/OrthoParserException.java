package ca.phon.orthography.parser.exceptions;

public class OrthoParserException extends RuntimeException {

	private static final long serialVersionUID = 8246804092533836151L;

	/**
	 * Position in line of error
	 */
	private int positionInLine = -1;
	
	/**
	 * Line number (default = 0)
	 */
	private int lineNumber = 0;

	public OrthoParserException() {
		super();
	}

	public OrthoParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrthoParserException(String message) {
		super(message);
	}

	public OrthoParserException(Throwable cause) {
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
