/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
