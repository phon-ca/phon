/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipa.parser;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import ca.phon.ipa.parser.exceptions.IPAParserException;
import ca.phon.ipa.parser.exceptions.InvalidTokenException;
import ca.phon.syllable.SyllableConstituentType;


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
				if(scType == null) {
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
					IPAParserException ex = new InvalidTokenException("Invalid token '" + currentChar + "'");
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
