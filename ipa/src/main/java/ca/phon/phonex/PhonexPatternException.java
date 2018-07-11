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
package ca.phon.phonex;

import org.antlr.runtime.tree.TreeNodeStream;

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
