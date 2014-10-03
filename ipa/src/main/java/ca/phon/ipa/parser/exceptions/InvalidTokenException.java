package ca.phon.ipa.parser.exceptions;

public class InvalidTokenException extends IPAParserException {

	private static final long serialVersionUID = 2778614459940783158L;

	public InvalidTokenException() {
		super();
	}

	public InvalidTokenException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTokenException(String message) {
		super(message);
	}

	public InvalidTokenException(Throwable cause) {
		super(cause);
	} 

}
