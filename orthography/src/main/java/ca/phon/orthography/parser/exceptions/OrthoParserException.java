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
package ca.phon.orthography.parser.exceptions;

public class OrthoParserException extends RuntimeException {

	private static final long serialVersionUID = 8246804092533836151L;

	/**
	 * Position in line of error
	 */
	private int positionInLine = -1;
	
	/**
	 * Line number (default = 0)
	 */
	private int lineNumber = 0;

	public OrthoParserException() {
		super();
	}

	public OrthoParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrthoParserException(String message) {
		super(message);
	}

	public OrthoParserException(Throwable cause) {
		super(cause);
	}

	public int getPositionInLine() {
		return positionInLine;
	}

	public void setPositionInLine(int positionInLine) {
		this.positionInLine = positionInLine;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
}
