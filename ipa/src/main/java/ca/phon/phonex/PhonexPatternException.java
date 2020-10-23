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
package ca.phon.phonex;

import org.antlr.runtime.tree.*;

/**
 * Exception for phonex pattern errors.
 *
 */
public class PhonexPatternException extends RuntimeException {

	private static final long serialVersionUID = 5982555937562885148L;
	
	private TreeNodeStream nodeStream;
	
	private int line = -1;
	
	private int charInLine = -1;
	
	public PhonexPatternException(int line, int charInLine) {
		super();
		this.line = line;
		this.charInLine = charInLine;
	}

	public PhonexPatternException(int line, int charInLine, String message, Throwable cause) {
		super(message, cause);
		this.line = line;
		this.charInLine = charInLine;
	}

	public PhonexPatternException(int line, int charInLine, String message) {
		super(message);
		this.line = line;
		this.charInLine = charInLine;
	}

	public PhonexPatternException(int line, int charInLine, Throwable cause) {
		super(cause);
		this.line = line;
		this.charInLine = charInLine;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getCharInLine() {
		return charInLine;
	}

	public void setCharInLine(int charInLine) {
		this.charInLine = charInLine;
	}
	
}
