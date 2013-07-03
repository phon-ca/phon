package ca.phon.util;

/**
 * A {@link Throwable} that's used for passing stack trace
 * info to the logger.
 * 
 * E.g.,
 * 
 * <code>
 * java.util.logging.Logger LOGGER = Logger.getLogger(getClass().getName());
 * LOGGER.log(Level.INFO, "Hello world", new StackTraceInfo());
 * </code>
 */
public class StackTraceInfo extends Exception {
	
	private static final long serialVersionUID = -840221233821072316L;

	public StackTraceInfo() {
		super();
	}

	public StackTraceInfo(String message, Throwable cause) {
		super(message, cause);
	}

	public StackTraceInfo(String message) {
		super(message);
	}

	public StackTraceInfo(Throwable cause) {
		super(cause);
	}
	
}
