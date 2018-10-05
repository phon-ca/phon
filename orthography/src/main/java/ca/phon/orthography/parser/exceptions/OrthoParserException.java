/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
