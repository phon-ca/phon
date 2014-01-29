package ca.phon.phonex;

/**
 * Exception for phonex pattern errors.
 *
 */
public class PhonexPatternException extends RuntimeException {

	private static final long serialVersionUID = 5982555937562885148L;

	public PhonexPatternException() {
		super();
	}

	public PhonexPatternException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhonexPatternException(String message) {
		super(message);
	}

	public PhonexPatternException(Throwable cause) {
		super(cause);
	}

}
