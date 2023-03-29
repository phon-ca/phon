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

import ca.phon.orthography.*;
import org.antlr.runtime.*;

import java.util.*;

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
		Token retVal = null;
		
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
				final OrthoPunctType pt = OrthoPunctType.fromChar(currentChar);
				if(pt != null) {
					if(pt == OrthoPunctType.AMPERSTAND && !Character.isWhitespace(nextChar)) {
						// word-fragment, read word
						readWord();
						return nextToken();
					} else {
						// setup PUNCT token
						retVal = new CommonToken(tokens.getTokenType("PUNCT"));
						retVal.setText(currentChar + "");
						retVal.setCharPositionInLine(cIndex);
						cIndex++;
					}
				}
				
				if(retVal == null) {
					// start of comment
					if(currentChar == '(') {
						cIndex++;
						readComment();
						return nextToken();
					// start of event
					} else if(currentChar == '*') {
						cIndex++;
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
		else
			retVal = new CommonToken(CommonToken.EOF);
		
		return retVal;
	}

	private void readComment() {
		final StringBuffer buffer = new StringBuffer();
		
		String type = null;
		boolean hadSlash = false;
		boolean finished = false;
		int parenIdx = 0;
		
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
				
			case '(':
				if(hadSlash) {
					hadSlash = false;
				} else {
					parenIdx++;
				}
				buffer.append(c);
				break;
				
			case ')':
				if(hadSlash) {
					hadSlash = false;
					buffer.append(c);
					break;
				} else {
					if(parenIdx > 0) {
						--parenIdx;
						buffer.append(c);
					} else {
						finished = true;
					}
					break;
				}
				
			case ':':
				// type is defined before first ':'
				if(type == null) {
					type = buffer.toString();
					buffer.setLength(0);
				} else 
					buffer.append(c);
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
		if(cIndex < data.length && data[cIndex] == ')') {
			cIndex++;
			if(type != null) {
				CommonToken typeToken = new CommonToken(tokens.getTokenType("COMMENT_TYPE"));
				typeToken.setText(type);
				
				tokenQueue.add(typeToken);
			}
			
			CommonToken commentToken = new CommonToken(tokens.getTokenType("COMMENT"));
			commentToken.setText(commentText);
			tokenQueue.add(commentToken);
		} else {
			CommonToken errToken = new CommonToken(tokens.getTokenType("ERROR"));
			errToken.setText("Incomplete comment - insert ')'");
			errToken.setCharPositionInLine(cIndex);
			tokenQueue.add(errToken);
		}
		
	}
	
	private void readEvent() {
		final StringBuffer evtBuffer = new StringBuffer();
		String type = null;
		
		boolean insideElement = false;
		while(cIndex < data.length && data[cIndex] != '*') {
			final char c = data[cIndex++];
			if(c == ':' && !insideElement && type == null) {
				type = evtBuffer.toString();
				evtBuffer.setLength(0);
			} else {
				evtBuffer.append(c);
				if(c == '(') {
					insideElement = true;
				} else if(c == ')') {
					insideElement = false;
				}
			}
		}
		
		if(cIndex < data.length && data[cIndex] == '*') {
			cIndex++;
			if(type != null) {
				CommonToken typeToken = new CommonToken(tokens.getTokenType("EVENT_TYPE"));
				typeToken.setText(type);
				tokenQueue.add(typeToken);
			}
			
			CommonToken evtToken = new CommonToken(tokens.getTokenType("EVENT"));
			evtToken.setText(evtBuffer.toString());
			tokenQueue.add(evtToken);
		} else {
			// add error token
			CommonToken errToken = new CommonToken(tokens.getTokenType("ERROR"));
			errToken.setText("Incomplete event - insert '*'");
			errToken.setCharPositionInLine(cIndex);
			tokenQueue.add(errToken);
		}
		
	}
	
	private void readWord() {
		final StringBuffer buffer = new StringBuffer();
		
		while(cIndex < data.length && !Character.isWhitespace(data[cIndex])
				&& data[cIndex] != '('
				&& data[cIndex] != '*'
				&& data[cIndex] != '~') {
			char c = data[cIndex];
			if(c == '+' && (buffer.length() == 0 || buffer.charAt(buffer.length()-1) != '&')) {
				break;
			}
			++cIndex;
			buffer.append(c);
		}
		
		// check for word prefix codes
		WordPrefixType wpt = null;
		for(WordPrefixType prefix:WordPrefixType.values()) {
			if(buffer.toString().startsWith(prefix.getCode())) {
				wpt = prefix;
				buffer.delete(0, wpt.getCode().length());
				break;
			}
		}
		
		// check for word suffix
		WordSuffixType wst = null;
		String formSuffix = null;
		String code = null;
		final int wsIdx = buffer.lastIndexOf("@");
		if(buffer.indexOf("@") > 0) {
			String suffixVal = buffer.substring(wsIdx);
			if(suffixVal.indexOf('-') > 0) {
				formSuffix = suffixVal.substring(suffixVal.indexOf('-')+1);
				suffixVal = suffixVal.substring(0, suffixVal.indexOf('-'));
			}
			if(suffixVal.indexOf(':') > 0) {
				code = suffixVal.substring(suffixVal.indexOf(':')+1);
				suffixVal = suffixVal.substring(0, suffixVal.indexOf(':'));
			}
			wst = WordSuffixType.fromCode(suffixVal);
			if(wst != null) {
				buffer.delete(wsIdx, buffer.length());
			}
		}
		
		// separated-prefix
		if(buffer.toString().endsWith("#")) {
			wst = WordSuffixType.SEPARATED_PREFIX;
			buffer.delete(buffer.length()-1, buffer.length());
		}
		
		final String word = buffer.toString();
		
		// setup tokens
		if(wpt != null) {
			WordPrefix wordPrefix = new WordPrefix(wpt);
			CommonToken wpToken = new WordPrefixToken(wordPrefix);
			tokenQueue.add(wpToken);
		}
		
		CommonToken wordToken = new CommonToken(tokens.getTokenType("WORD"));
		wordToken.setText(word);
		tokenQueue.add(wordToken);
		
		if(wst != null) {
			WordSuffix wordSuffix = new WordSuffix(wst, formSuffix, code);
			CommonToken wsToken = new WordSuffixToken(wordSuffix);
			tokenQueue.add(wsToken);
		}
	}
	
	public static class WordPrefixToken extends CommonToken {

		WordPrefix prefix;
		
		public WordPrefixToken(WordPrefix prefix) {
			super((new OrthoTokens()).getTokenType("WORD_PREFIX"));
			this.prefix = prefix;
		}
		
		public WordPrefix getWordPrefix() {
			return this.prefix;
		}
		
	}
	
	public static class WordSuffixToken extends CommonToken {

		WordSuffix suffix;
		
		public WordSuffixToken(WordSuffix suffix) {
			super((new OrthoTokens()).getTokenType("WORD_SUFFIX"));
			this.suffix = suffix;
		}
		
		public WordSuffix getWordSuffix() {
			return this.suffix;
		}
	}
}
