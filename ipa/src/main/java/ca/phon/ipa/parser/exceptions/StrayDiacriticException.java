package ca.phon.ipa.parser.exceptions;

public class StrayDiacriticException extends IPAParserException {

	private static final long serialVersionUID = -8387804280337162737L;

	public StrayDiacriticException() {
		super();
	}

	public StrayDiacriticException(String message, Throwable cause) {
		super(message, cause);
	}

	public StrayDiacriticException(String message) {
		super(message);
	}

	public StrayDiacriticException(Throwable cause) {
		super(cause);
	}

}
