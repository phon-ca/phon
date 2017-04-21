/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
