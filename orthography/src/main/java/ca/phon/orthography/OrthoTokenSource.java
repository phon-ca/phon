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

import java.util.LinkedList;
import java.util.Queue;

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
	
	private Queue<CommonToken> tokenQueue = new LinkedList<CommonToken>();
	
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
		
		if(tokenQueue.peek() != null) {
			retVal = tokenQueue.poll();
		} else {
			if(cIndex < data.length) {
				// this method is always called on a new token boundary
				char currentChar = data[cIndex];
				// assume word-boundary at end
				char nextChar = ( (cIndex+1) < data.length ? data[cIndex+1] : ' ' );
				
				// punctuation must be separated by a space
				// check punctuation first
				if(Character.isWhitespace(nextChar) &&
					!Character.isWhitespace(currentChar) ) {
					for(OrthoPunctType pt:OrthoPunctType.values()) {
						if(pt.getChar() == currentChar) {
							// setup PUNCT token
							retVal = new CommonToken(tokens.getTokenType("PUNCT"));
							retVal.setText(currentChar + "");
							retVal.setCharPositionInLine(cIndex);
							cIndex++;
						}
					}
				}
				
				if(retVal == null) {
					// start of comment
					if(currentChar == '(') {
						readComment();
						return nextToken();
					// start of event
					} else if(currentChar == '*') {
						readEvent();
						return nextToken();
					// wordnet
					} else if(currentChar == '+' || currentChar == '~') {
						//make sure wordnets are their own word in Phon
						retVal = new CommonToken(tokens.getTokenType("WORDNET_MARKER"));
						retVal.setText(currentChar+"");
						retVal.setCharPositionInLine(cIndex);
						cIndex++;
					// word boundary - move ahead and return next token
					} else if(Character.isWhitespace(currentChar)) { 
						cIndex++;
						return nextToken();
					// everything else is a word
					} else {
						readWord();
						return nextToken();
					}
				}
				
			}
		}
		
		if(retVal != null) 
			retVal.setTokenIndex(tokenIndex++);
		
		return retVal;
	}

	private void readComment() {
		final StringBuffer buffer = new StringBuffer();
		
		String type = null;
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
				
			case ':':
				type = buffer.toString();
				buffer.setLength(0);
				break;
				
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
		
		if(type == null) {
			CommonToken typeToken = new CommonToken(tokens.getTokenType("COMMENT_TYPE"));
			typeToken.setText(type);
			
			tokenQueue.add(typeToken);
		}
		
		CommonToken commentToken = new CommonToken(tokens.getTokenType("COMMENT"));
		commentToken.setText(commentText);
		tokenQueue.add(commentToken);
	}
	
	private void readEvent() {
		final StringBuffer evtBuffer = new StringBuffer();
		String type = null;
		
		while(cIndex < data.length && data[cIndex] != '*'
			&& data[cIndex] != ']') {
			if(data[cIndex] == ':') {
				type = evtBuffer.toString();
				evtBuffer.setLength(0);
			} else {
				evtBuffer.append(data[cIndex]);
			}
		}
		
		if(cIndex < data.length && data[cIndex] == '*') cIndex++;
		
		if(type != null) {
			CommonToken typeToken = new CommonToken(tokens.getTokenType("EVENT_TYPE"));
			typeToken.setText(type);
			tokenQueue.add(typeToken);
		}
		
		CommonToken evtToken = new CommonToken(tokens.getTokenType("EVENT"));
		evtToken.setText(evtBuffer.toString());
		tokenQueue.add(evtToken);
	}
	
	private void readWord() {
		String wText = "";
		
		while(cIndex < data.length && !Character.isWhitespace(data[cIndex])
				&& data[cIndex] != '('
				&& data[cIndex] != '*'
				&& data[cIndex] != ']'
				&& data[cIndex] != '+'
				&& data[cIndex] != '~') {
			wText += data[cIndex++];
		}
		
		CommonToken wordToken = new CommonToken(tokens.getTokenType("WORD"));
		wordToken.setText(wText);
		tokenQueue.add(wordToken);
	}
}
