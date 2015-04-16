/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

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
