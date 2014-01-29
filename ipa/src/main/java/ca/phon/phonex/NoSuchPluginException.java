package ca.phon.phonex;

import org.antlr.runtime.IntStream;

/**
 * Thrown during compilation when the specified plug-in 
 * is not available.
*/
public class NoSuchPluginException extends PhonexPatternException {

	private static final long serialVersionUID = -1787675745486985284L;

	public NoSuchPluginException() {
		super();
	}

	public NoSuchPluginException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchPluginException(String message) {
		super(message);
	}

	public NoSuchPluginException(Throwable cause) {
		super(cause);
	}
}
