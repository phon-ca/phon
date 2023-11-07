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
package ca.phon.orthography.parser;

public class OrthoParserException extends RuntimeException {

	public enum Type {
		Unknown,
		InvalidToken,
		OutOfPlace,
		StrayAnnotation,
		TerminatorAlreadySpecified,
		ContentAfterTerminator,
		MediaAlreadySpecified,
		InvalidTimeString,
		ReplacementWithoutContent,
		AnnotationWithoutContent,
		InvalidAnnotation,
		MissingOpenBracket,
		MissingCloseBracket,
		MissingGroupStart,
		MissingGroupEnd,
		MissingPgStart,
		MissingPgEnd,
		MissingMediaBullet
	}

	private Type type;

	/**
	 * Position in line of error
	 */
	private int positionInLine = -1;
	
	/**
	 * Line number (default = 0)
	 */
	private int lineNumber = 0;

	public OrthoParserException() {
		this(Type.Unknown);
	}

	public OrthoParserException(Type type) {
		super();
		this.type = type;
	}

	public OrthoParserException(String message, Throwable cause) {
		this(Type.Unknown, message, cause);
	}

	public OrthoParserException(Type type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public OrthoParserException(String message) {
		this(Type.Unknown, message);
	}

	public OrthoParserException(Type type, String message) {
		this(type, message, -1);
	}

	public OrthoParserException(String message, int charPositionInLine) {
		this(Type.Unknown, message, charPositionInLine);
	}

	public OrthoParserException(Type type, String message, int charPositionInLine) {
		super(message);
		this.type = type;
		this.positionInLine = charPositionInLine;
	}

	public OrthoParserException(Type type, String message, Throwable cause, int charPositionInLine) {
		super(message, cause);
		this.type = type;
		this.positionInLine = charPositionInLine;
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
