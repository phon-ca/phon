/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.log;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

/**
 * Holds a specified number of log events in memory.
 *
 */
public class LogHandler extends Handler {

	public final static String BUFFER_SIZE_PROP = LogHandler.class.getName() + ".bufferSize";
	
	private final static int DEFAULT_BUFFER_SIZE = 100;
	
	private final CircularFifoBuffer logBuffer;
	
	// setup a static reference to the most newly created object
	// for the application, this should only happen once during logging setup
	private static final AtomicReference<LogHandler> instanceRef = new AtomicReference<>();
	
	public static LogHandler getInstance() {
		return instanceRef.get();
	}
	
	public LogHandler() {
		super();
		logBuffer = new CircularFifoBuffer(DEFAULT_BUFFER_SIZE);
		instanceRef.set(this);
	}
	
	public CircularFifoBuffer getLogBuffer() {
		return this.logBuffer;
	}
	
	@Override
	public void publish(LogRecord record) {
		Level logLevel = record.getLevel();
		if(logLevel.intValue() >= getLevel().intValue())
			logBuffer.add(record);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
	
}
