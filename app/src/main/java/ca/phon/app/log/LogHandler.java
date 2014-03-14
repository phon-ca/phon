package ca.phon.app.log;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import ca.phon.util.PrefHelper;

/**
 * Holds a specified number of log events in memory.
 *
 */
public class LogHandler extends Handler {

	public final static String BUFFER_SIZE_PROP = LogHandler.class.getName() + ".bufferSize";
	
	private final static int DEFAULT_BUFFER_SIZE = 1000;
	
	private final CircularFifoBuffer logBuffer;
	
	public LogHandler() {
		super();
		
		logBuffer = new CircularFifoBuffer(DEFAULT_BUFFER_SIZE);
	}
	
	@Override
	public void publish(LogRecord record) {
		logBuffer.add(record);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
	
}
