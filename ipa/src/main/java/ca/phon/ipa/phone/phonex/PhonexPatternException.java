package ca.phon.ipa.phone.phonex;

/**
 * Unchecked exception thrown to indicate a syntax error 
 * in a regular-expression pattern. 
 */
public class PhonexPatternException extends IllegalArgumentException {

	private static final long serialVersionUID = 7034559683544852746L;

	public PhonexPatternException() {
		super();
	}

	public PhonexPatternException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public PhonexPatternException(String arg0) {
		super(arg0);
	}

	public PhonexPatternException(Throwable arg0) {
		super(arg0);
	}

}
