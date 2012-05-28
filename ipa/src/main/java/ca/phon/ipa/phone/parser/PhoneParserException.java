package ca.phon.ipa.phone.parser;

/**
 * Exception class for errors during Phone
 * lexing/parsing.
 * 
 */
public class PhoneParserException extends Exception {

	private static final long serialVersionUID = -3052112481299193424L;

	/**
	 * Position in line of error
	 */
	private int positionInLine = -1;
	
	/**
	 * Line number (default = 0)
	 */
	private int lineNumber = 0;

	public PhoneParserException() {
		super();
	}

	public PhoneParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhoneParserException(String message) {
		super(message);
	}

	public PhoneParserException(Throwable cause) {
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
