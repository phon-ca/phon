/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.orthography;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

/**
 * Chomps the given string into tokens usable
 * by the PhonTranscriptionParser
 *
 */
public class OrthoTokenSource implements TokenSource {
	
	/** The char array */
	private char[] data;
	
	/** char index */
	private int cIndex = 0;
	
	/** Token mapper */
	private OrthoTokens tokens;
	
	public OrthoTokenSource(String str) {
		this.data = str.toCharArray();
		this.cIndex = 0;
		tokens = new OrthoTokens();
	}

	@Override
	public String getSourceName() {
		return "";
	}

	int tokenIndex = 0;
	@Override
	public Token nextToken() {
		CommonToken retVal = null;
		
		if(cIndex < data.length) {
			char currentChar = data[cIndex];
			
			if(currentChar == '{') {
				retVal = new CommonToken(tokens.getTokenType("IG_START"));
				retVal.setText(currentChar+"");
				retVal.setCharPositionInLine(cIndex);
				cIndex++;
			} else if(currentChar == '}') {	
				retVal = new CommonToken(tokens.getTokenType("IG_END"));
				retVal.setText(currentChar+"");
				retVal.setCharPositionInLine(cIndex);
				cIndex++;
			} else if(currentChar == '[') {
				retVal = new CommonToken(tokens.getTokenType("G_START"));
				retVal.setText(currentChar+"");
				retVal.setCharPositionInLine(cIndex);
				cIndex++;
			} else if(currentChar == ']') {
				retVal = new CommonToken(tokens.getTokenType("G_END"));
				retVal.setText(currentChar+"");
				retVal.setCharPositionInLine(cIndex);
				cIndex++;
			} else if(currentChar == '(') {
				int startIndex = cIndex++;
				retVal = readComment();
				retVal.setCharPositionInLine(startIndex);
			} else if(currentChar == '*') {
				int startIndex = cIndex++;
				retVal = readEvent();
				retVal.setCharPositionInLine(startIndex);
			} else if(currentChar == '+') {
				//make sure wordnets are their own word in Phon
				retVal = new CommonToken(tokens.getTokenType("WORD"));
				retVal.setText(currentChar+"");
				retVal.setCharPositionInLine(cIndex);
				cIndex++;
			} else if(Character.isWhitespace(currentChar)) { 
				cIndex++;
				return nextToken();
			} else {
				int startIndex = cIndex;
				retVal = readWord();
				retVal.setCharPositionInLine(startIndex);
			}
			
		}
		
		if(retVal != null) 
			retVal.setTokenIndex(tokenIndex++);
		
		return retVal;
	}

	private CommonToken readComment() {
		final StringBuffer buffer = new StringBuffer();
		
		boolean hadSlash = false;
		boolean finished = false;
		for( ; cIndex < data.length; cIndex++) {
			final char c = data[cIndex];
			
			switch(c) {
			case '\\':
				if(hadSlash) {
					hadSlash = false;
				} else {
					hadSlash = true;
				}
				buffer.append(c);
				break;
				
			case ']':
			case '(':
			case ')':
				if(hadSlash) {
					hadSlash = false;
					buffer.append(c);
					break;
				} else {
					finished = true;
					break;
				}
				
			default:
				if(hadSlash) {
					// invalid escape sequence
					finished = true;
					break;
				}
				buffer.append(c);	
				break;
			}
			if(finished) break;
		}
		
		final String commentText = buffer.toString();
		if(cIndex < data.length && data[cIndex] == ')') cIndex++;
		
		CommonToken retVal = new CommonToken(tokens.getTokenType("COMMENT"));
		retVal.setText(commentText);
		return retVal;
	}
	
	private CommonToken readEvent() {
		String evtText = "";
		
		while(cIndex < data.length && data[cIndex] != '*'
			&& data[cIndex] != ']') {
			evtText += data[cIndex++];
		}
		
		if(cIndex < data.length && data[cIndex] == '*') cIndex++;
		
		CommonToken retVal = new CommonToken(tokens.getTokenType("EVENT"));
		retVal.setText(evtText);
		return retVal;
	}
	
	private CommonToken readWord() {
		String wText = "";
		
		while(cIndex < data.length && !Character.isWhitespace(data[cIndex])
				&& data[cIndex] != '('
				&& data[cIndex] != '*'
				&& data[cIndex] != ']'
				&& data[cIndex] != '+') {
			wText += data[cIndex++];
		}
		
		CommonToken retVal = new CommonToken(tokens.getTokenType("WORD"));
		retVal.setText(wText);
		return retVal;
	}
}
