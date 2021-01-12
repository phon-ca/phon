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
package ca.phon.ipa.parser;

import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.ipa.parser.exceptions.InvalidTokenException;
import org.antlr.runtime.*;

import ca.phon.syllable.*;


/**
 * <p>Tokenize IPA strings for an ANTLR parser.</p>
 * 
 * 
 */
public class IPALexer implements TokenSource {

	/**
	 * Token mapper
	 * 
	 */
	private IPATokens tokenMapper;
	
	/**
	 * Source string
	 * 
	 */
	private String source;
	
	/**
	 * Current position
	 */
	private int currentPosition = 0;
	
	/**
	 * Have we returned at least one non-space token in nextToken
	 */
	private boolean hasReturnedToken = false;
	
	/**
	 * Set to true when the next token expected is a 
	 * syllable constituent type identifier
	 */
	private boolean expectingScType = false;
	
	private boolean expectingGroupReference = false;
	
	/**
	 * Constructor
	 * 
	 * @param string the string to tokenize
	 */
	public IPALexer(String string) {
		this.source = string;
		this.currentPosition = 0;
		
		tokenMapper = IPATokens.getSharedInstance();
	}

	/**
	 * @return
	 */
	@Override
	public Token nextToken() {
		Token retVal = null;
		
		while(retVal == null && currentPosition < source.length()) {
			char currentChar = source.charAt(currentPosition);
			IPATokenType tokenType = tokenMapper.getTokenType(currentChar);
			
			if(tokenType == IPATokenType.SPACE) {
				// move to next word character, ignoring any remaining whitespace
				currentPosition = nextWordStart();
				if(hasReturnedToken && currentPosition < source.length()) {
					// queue space token
					retVal = new CommonToken(tokenMapper.getTypeValue(IPATokenType.SPACE));
				}
				continue;
			}
			
			if(expectingScType) {
				final SyllableConstituentType scType = SyllableConstituentType.fromString(currentChar+"");
				if(scType == SyllableConstituentType.WORDBOUNDARYMARKER
						|| scType == SyllableConstituentType.SYLLABLEBOUNDARYMARKER
						|| scType == SyllableConstituentType.SYLLABLESTRESSMARKER) {
					IPAParserException ex = new IPAParserException("Invalid syllable constituent type '" +
							currentChar + "'");
					ex.setPositionInLine(currentPosition);
					throw ex;
				} else {
					int antlrType = tokenMapper.getTypeValue(IPATokenType.SCTYPE);
					retVal = new CommonToken(antlrType, currentChar+"");
					retVal.setCharPositionInLine(currentPosition);
				}
				expectingScType = false;
			} else if(expectingGroupReference) {
				final StringBuffer buffer = new StringBuffer();
				int startPos = currentPosition;
				while(tokenType != IPATokenType.CLOSE_BRACE && currentPosition < source.length()) {
					buffer.append(currentChar);
					currentChar = source.charAt(++currentPosition);
					tokenType = tokenMapper.getTokenType(currentChar);
				}
				
				int antlrType = tokenMapper.getTypeValue(IPATokenType.GROUP_NAME);
				retVal = new CommonToken(antlrType, buffer.toString());
				retVal.setCharPositionInLine(startPos);
				
				expectingGroupReference = false;
				
				// returrn close brace as next token
				if(tokenType == IPATokenType.CLOSE_BRACE)
					--currentPosition;
			} else {
				if(tokenType == null) {
					InvalidTokenException ex = new InvalidTokenException("Invalid token '" + currentChar + "'");
					ex.setPositionInLine(currentPosition);
					throw ex;
				} else {
					int antlrType = tokenMapper.getTypeValue(tokenType);
					
					retVal = new CommonToken(antlrType, currentChar+"");
					retVal.setCharPositionInLine(currentPosition);
					
					if(tokenType == IPATokenType.COLON) {
						expectingScType = true;
					} else if(tokenType == IPATokenType.OPEN_BRACE) {
						expectingGroupReference = true;
					}
				}
				
			}
			currentPosition++;
		}
		
		if(retVal == null) {
			retVal = new CommonToken(CommonToken.EOF);
		}
		
		hasReturnedToken = true;
		return retVal;
	}
	
	private int nextWordStart() {
		int retVal = currentPosition;
		while(retVal < source.length()) {
			char c = source.charAt(retVal);
			if(Character.isWhitespace(c)) {
				++retVal;
				continue;
			} else {
				break;
			}
		}
		return retVal;
	}

	@Override
	public String getSourceName() {
		return "";
	}

}
