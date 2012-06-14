package ca.phon.ipa.parser;

/**
 * Exception class for errors during Phone
 * lexing/parsing.
 * 
 */
public class IPAParserException extends Exception {

	private static final long serialVersionUID = -3052112481299193424L;

	/**
	 * Position in line of error
	 */
	private int positionInLine = -1;
	
	/**
	 * Line number (default = 0)
	 */
	private int lineNumber = 0;

	public IPAParserException() {
		super();
	}

	public IPAParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public IPAParserException(String message) {
		super(message);
	}

	public IPAParserException(Throwable cause) {
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
