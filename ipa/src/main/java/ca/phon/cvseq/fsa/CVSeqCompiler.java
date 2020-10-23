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
package ca.phon.cvseq.fsa;

import java.text.*;

import org.apache.logging.log4j.*;

import ca.phon.cvseq.*;
import ca.phon.fsa.*;
import de.susebox.jtopas.*;

/**
 *
 */
public class CVSeqCompiler {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(CVSeqCompiler.class.getName());
	
	private enum Quantifier {
		ZeroOrOne,
		ZeroOrMore,
		OneOrMore;
		
		private char[] images = {
				'?',
				'*',
				'+'
		};
		
		public static char getImage(Quantifier q) {
			return q.getImage();
		}
		
		public char getImage() {
			return images[ordinal()];
		}
	}
	
	/** The state prefix */
	private final String statePrefix = "q";
	/** Variable for keeping track of state number - reset on call to compile(String) */
	private int stateIndex = 0;
	/** The current matcher string */
	private String currentMatcher;
	
	public CVSeqCompiler() {
		super();
	}
	
	public SimpleFSA<CVSeqType> compile(String matcherString) 
		throws ParseException {
	
		stateIndex = 0;
		currentMatcher = matcherString;
		
		Tokenizer tokenizer = getTokenizer();
		TokenizerSource source = new StringSource(matcherString);
		tokenizer.setSource(source);
		
		return tokensToFSA(tokenizer);
	}
	
	private SimpleFSA<CVSeqType> tokensToFSA(Tokenizer tokenizer) 
		throws ParseException {
		// create the machine and add initial state
		SimpleFSA<CVSeqType> fsa = new SimpleFSA<CVSeqType>();
		String initialState = getNextStateName();
		fsa.addState(initialState);
		fsa.setInitialState(initialState);
		
		// test first token
		Token token = null;
		try {
			token = tokenizer.nextToken();
		} catch (TokenizerException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		if(token != null
				&& !token.getImage().equals("#"))
			tokenizer.setReadPositionAbsolute(0);
		
		CVSeqType currentType = null;
		while((currentType = readMatcher(tokenizer)) != null) {
			newTransition(fsa, currentType);
			
			// look for quantification
			// attempt to read a quantifier
			if(tokenizer.hasMoreToken()) {
				Token nextToken = null;
				try {
					nextToken = tokenizer.nextToken();
				} catch (TokenizerException e) {
					throw new ParseException(currentMatcher,
							tokenizer.getReadPosition());
				}
				
				if(nextToken.getCompanion() != null) {
					if(nextToken.getCompanion() instanceof Quantifier) {
						// handle quantifier
						Quantifier q = (Quantifier)nextToken.getCompanion();
						if(q == Quantifier.OneOrMore)
							makeOneOrMore(fsa, currentType);
						else if(q == Quantifier.ZeroOrMore)
							makeZeroOrMore(fsa, currentType);
						else if(q == Quantifier.ZeroOrOne)
							makeZeroOrOne(fsa, currentType);
						else
							// should never get here
							throw new ParseException(currentMatcher, tokenizer.getReadPosition());
						
					} else {
						// reset tokenzier position
						tokenizer.setReadPositionRelative(-1*nextToken.getImage().length());
					}
				} else {
					if(nextToken.getType() == Token.EOF)
						break;
					else if(nextToken.getType() == Token.WHITESPACE) {
						// should actually be a WordBoundary type
						// reset position
						tokenizer.setReadPositionRelative(-1*nextToken.getImage().length());
					} else if(nextToken.getImage().equals("#")) { 
						makeHashAtEnd(fsa);
						break;
					} else {
						// token should not be here without a companion otherwise
						// throw an exception
						throw new ParseException(currentMatcher, tokenizer.getReadPosition());
					}
				}
			}
		}
		
		return fsa;
	}
	
	private static void makeZeroOrOne(SimpleFSA<CVSeqType> fsa, CVSeqType matcher) {
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:fsa.getFinalStates()) {
			// get transtions to the final state
			for(FSATransition<CVSeqType> trans:fsa.getTransitionsToState(finalState)) {
				fsa.addFinalState(trans.getFirstState());
			}
		}
	}

	private void makeZeroOrMore(SimpleFSA<CVSeqType> fsa, CVSeqType matcher) {
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:fsa.getFinalStates()) {
			// get transtions to the final state
			for(FSATransition<CVSeqType> trans:fsa.getTransitionsToState(finalState)) {
				fsa.addFinalState(trans.getFirstState());
			}
			
			CVSeqTransition	transition = new CVSeqTransition(matcher);
			transition.setFirstState(finalState);
			transition.setToState(finalState);
			fsa.addTransition(transition);
		}
	}
	
	private void makeOneOrMore(SimpleFSA<CVSeqType> fsa, CVSeqType matcher) {
		// make a new state
		String newState = getNextStateName();
		fsa.addState(newState);
		
		CVSeqTransition	transition = new CVSeqTransition(matcher);
		transition.setFirstState(newState);
		transition.setToState(newState);
		fsa.addTransition(transition);
		
		for(String finalState:fsa.getFinalStates()) {
			transition = null;
			
			transition = new CVSeqTransition(matcher);
			transition.setFirstState(finalState);
			transition.setToState(newState);
			fsa.addTransition(transition);
		}
		
		fsa.addFinalState(newState);
	}
	
	private void makeHashAtEnd(SimpleFSA<CVSeqType> fsa) {
		// add the anything matcher to all final states
		String newState = getNextStateName();
		
//		FeatureSetMatcher fsm = new FeatureSetMatcher();
		
		fsa.addState(newState);
		
		for(String finalState:fsa.getFinalStates()) {
			CVSeqTransition smt = new CVSeqTransition(CVSeqType.DontCare);
			smt.setFirstState(finalState);
			smt.setToState(newState);
			fsa.addTransition(smt);
		}
	}
	
	/**
	 * This method does the following:
	 *  * removes all current final states
	 *  * creates a new state
	 *  * create a transition from all old final states to the new one
	 *  * makes the new state final
	 * @param fsa
	 * @param matcher the matcher to use for transitions
	 */
	private void newTransition(SimpleFSA<CVSeqType> fsa, CVSeqType matcher) {
		// strip final states
		String[] oldFinals = stripFinalStates(fsa);
		
		// create a new state and make it final
		String newState = getNextStateName();
		fsa.addState(newState);
		fsa.addFinalState(newState);
		
		for(String oldFinal:oldFinals) {
			CVSeqTransition transition = new CVSeqTransition(matcher);
			transition.setFirstState(oldFinal);
			transition.setToState(newState);
			
			fsa.addTransition(transition);
		}
		
		if(oldFinals.length == 0) {
			CVSeqTransition	transition = new CVSeqTransition(matcher);
			transition.setFirstState(fsa.getInitialState());
			transition.setToState(newState);
			
			fsa.addTransition(transition);
		}
	}
	
	/**
	 * Strips the final states from the fsa and returns the
	 * stripped values.
	 * 
	 * @param fsa
	 */
	private static String[] stripFinalStates(SimpleFSA<CVSeqType> fsa) {
		String[] currentFinals = fsa.getFinalStates();
		for(String finalState:currentFinals)
			fsa.removeFinalState(finalState);
		return currentFinals;
	}
	
	private CVSeqType readMatcher(Tokenizer tokenizer) 
		throws ParseException {
		if(!tokenizer.hasMoreToken())
			return null;
		
		Token token = null;
		try {
			token = tokenizer.nextToken();
		} catch (TokenizerException e) {
			throw new ParseException(currentMatcher, 
					tokenizer.getReadPosition());
		}
		
		if(token.getImage().length() == 0)
			return null;
		
		if(token.getImage().equals("#"))
			return null;
		
		if(token.getCompanion() != null) {
			if(token.getCompanion() instanceof CVSeqType) {
				return (CVSeqType)token.getCompanion();
			} else {
				throw new ParseException(currentMatcher, tokenizer.getReadPosition());
			}
		} else {
			if(token.getType() == Token.WHITESPACE) {
				return CVSeqType.WordBoundary;
			} else 
				throw new ParseException(currentMatcher, tokenizer.getReadPosition());
		}
	}
	
	/**
	 * Returns the next state name
	 * 
	 */
	private String getNextStateName() {
		return statePrefix + (stateIndex++);
	}

	/**
	 * Get the tokenizer
	 */
	private Tokenizer getTokenizer() {
		TokenizerProperties props = new StandardTokenizerProperties();
		
		int parseFlags = 
			Flags.F_COUNT_LINES | // count lines and cols
			Flags.F_NO_CASE | // case insensitive
			Flags.F_RETURN_SIMPLE_WHITESPACES; // spaces are important
		
		props.setParseFlags(parseFlags);
		
		props.addSpecialSequence("#");
		
		// add stress matcher type
		for(CVSeqType stType:CVSeqType.values()) {
			props.addSpecialSequence(""+stType.getImage(), stType);
		}
		
		// add quantifiers
		for(Quantifier q:Quantifier.values()) {
			props.addSpecialSequence(""+q.getImage(), q);
		}
		
		return new StandardTokenizer(props);
	}
}
