package ca.phon.audio;

public class UnsupportedFormatException extends AudioIOException {

	private static final long serialVersionUID = -6035936792592208704L;

	public UnsupportedFormatException() {
		super();
	}

	public UnsupportedFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedFormatException(String message) {
		super(message);
	}

	public UnsupportedFormatException(Throwable cause) {
		super(cause);
	}

}
