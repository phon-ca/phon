package ca.phon.audio;

public class AudioIOException extends Exception {

	private static final long serialVersionUID = -4052525530828167525L;

	public AudioIOException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AudioIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AudioIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public AudioIOException(String message) {
		super(message);
	}

	public AudioIOException(Throwable cause) {
		super(cause);
	}

}
