package ca.phon.script;

/**
 * Wrapper exection for {@link PhonScript}s
 * 
 */
public class PhonScriptException extends Exception {

	private static final long serialVersionUID = -2330681946712142298L;

	public PhonScriptException() {
		super();
	}

	public PhonScriptException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PhonScriptException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhonScriptException(String message) {
		super(message);
	}

	public PhonScriptException(Throwable cause) {
		super(cause);
	}
	
}
