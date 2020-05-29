package ca.phon.audio;

public class InvalidHeaderException extends AudioIOException {

	private static final long serialVersionUID = 3349351994917897980L;

	public InvalidHeaderException() {
		super();
	}

	public InvalidHeaderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidHeaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidHeaderException(String message) {
		super(message);
	}

	public InvalidHeaderException(Throwable cause) {
		super(cause);
	}

}
