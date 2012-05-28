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
package ca.phon.syllable.pattern;

import ca.phon.exceptions.ParserException;
import ca.phon.phone.PhoneSequenceMatcher;
import ca.phon.syllable.SyllableConstituentType;
import de.susebox.jtopas.Flags;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.StringSource;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;

/**
 * Compiles syllable matchers.
 */
public class SyllablePattern {
	private static String currentString;
	
	/** The compiled matcher */
	private final SyllableMatcher matcher;

	/**
	 * Constructor
	 */
	private SyllablePattern(SyllableMatcher matcher) {
		super();
		
		this.matcher = matcher;
	}
	
	/**
	 * Create the tokenizer
	 */
	private static Tokenizer getTokenizer() {
		TokenizerProperties props = new StandardTokenizerProperties();
		
		int parseFlags = 
			Flags.F_COUNT_LINES | // count lines and cols
			Flags.F_NO_CASE; // case insensitive
		props.setParseFlags(parseFlags);
		
		for(SyllableConstituentType scType:SyllableConstituentType.values()) {
			String pattern = scType.getIdentifier() + "[0-9]?";
			props.addPattern(pattern);
		}
		
		props.addSpecialSequence("="); // equals
		props.addSpecialSequence("_"); // nothing
		
		props.removeSeparators(props.getSeparators());
		props.addSeparators(":");
		
		return new StandardTokenizer(props);
	}
	
	/**
	 * Return a SyllableMatcher
	 * 
	 * @param syllExpr
	 * @return SyllableMatcher
	 * @throws ParserException
	 */
	public static SyllableMatcher compile(String syllExpr) 
		throws ParserException {
		currentString = syllExpr;
		Tokenizer tokenizer = getTokenizer();
		tokenizer.setSource(new StringSource(syllExpr));
		
		return readMatcher(tokenizer);
	}
	
	/* Parser */
	private static SyllableMatcher readMatcher(Tokenizer tokenizer) 
		throws ParserException {
//		try {
//			if(charQueue.peek().charValue() == EMPTY_CHAR)
//				return new EmptySyllableMatcher();
//			else if(charQueue.peek().charValue() == '{')
//				return readAllMatcher(charQueue);
//			else
//				return readConstituentMatcher(charQueue);
//		} catch (EmptyQueueException e) {
//			throw new ParserException("Unexpected end of input.");
//		}
		// get the first token
		try {
			Token token = tokenizer.nextToken();
			if(token.getType() == Token.PATTERN) {
				// reset pos
				tokenizer.setReadPositionRelative(-1*token.getImage().length());
				// read consitutient matchers
				return readConstituentMatcher(tokenizer);
			} else if(token.getType() == Token.NORMAL) {
				if(token.getImage().equals("{}"))
					return new AnySyllableMatcher();
				else
					throw new ParserException(currentString, tokenizer.getReadPosition(), "Expected: '{'");
			} else if(token.getType() == Token.SPECIAL_SEQUENCE) {
				if(token.getImage().equals("_")) 
					return new EmptySyllableMatcher();
				else
					throw new ParserException(currentString, tokenizer.getReadPosition(), "Expected: '_'");
			} else {
				throw new ParserException(currentString, tokenizer.getReadPosition(), "Invalid Input");
			}
		} catch (TokenizerException e) {
			throw new ParserException(currentString, tokenizer.getReadPosition(), e.getMessage());
		}
	}
	
	private static SyllableConstituentMatcher readConstituentMatcher(Tokenizer tokenizer) 
		throws ParserException {
		try {
			Token token = null;
			SyllableConstituentMatcher retVal = new SyllableConstituentMatcher();
			while(tokenizer.hasMoreToken()) {
				// read identifier
				token = tokenizer.nextToken();
				
				if(token.getImage().length() == 0)
					break;
				
				if(token.getType() == Token.SEPARATOR) // read seperator
					token = tokenizer.nextToken();
				
				if(token.getType() != Token.PATTERN) {
					throw new ParserException(currentString,
							tokenizer.getReadPosition(), "Expected on of: 'LA', 'O', 'N', 'C', 'RA', 'OEHS'");
				}
				
				String identifier = token.getImage();
				
				// now read the 'equals' sign
				token = tokenizer.nextToken();
				if(!token.getImage().equals("=")) {
					throw new ParserException(currentString, tokenizer.getReadPosition(),
							"Expected: '='");
				}
				
				// read the matcher
				token = tokenizer.nextToken();
				if(token.getType() != Token.NORMAL && !token.getImage().equals("_")) {
					throw new ParserException(currentString, tokenizer.getReadPosition());
				}
				
				
				String psMatcher = token.getImage();
				PhoneSequenceMatcher phoneSeqMatcher = 
					PhoneSequenceMatcher.compile(psMatcher);
				
				retVal.addMatcher(identifier, phoneSeqMatcher);
			}
			return retVal;
		} catch (TokenizerException e) {
			throw new ParserException(currentString, tokenizer.getReadPosition(), e.getMessage());
		}
	}
	
	/* End Parser */
}
