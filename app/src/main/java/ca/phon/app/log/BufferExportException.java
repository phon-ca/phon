package ca.phon.app.log;

public class BufferExportException extends Exception {

	private static final long serialVersionUID = 8864838901456302535L;
	
	private LogBuffer buffer;
	
	private long location;

	public BufferExportException() {
		super();
	}

	public BufferExportException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BufferExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public BufferExportException(String message) {
		super(message);
	}

	public BufferExportException(Throwable cause) {
		super(cause);
	}

	public LogBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(LogBuffer buffer) {
		this.buffer = buffer;
	}

	public long getLocation() {
		return location;
	}

	public void setLocation(long location) {
		this.location = location;
	}
	
}
