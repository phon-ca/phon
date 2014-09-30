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
package ca.phon.stresspattern.fsa;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.fsa.FSATransition;
import ca.phon.fsa.SimpleFSA;
import ca.phon.stresspattern.StressMatcherType;
import de.susebox.jtopas.Flags;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.StringSource;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;
import de.susebox.jtopas.TokenizerSource;

/**
 * Compiles a given stress pattern string into
 * a finite state automata.
 *
 */
public class StressPatternCompiler {
	
	private static final Logger LOGGER = Logger
			.getLogger(StressPatternCompiler.class.getName());
	
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
	
//	public static SimpleFSA<StressMatcherType> compile(String matcherString) {
//		StressPatternCompiler compiler = new StressPatternCompiler();
//		return compiler.compile(matcherString);
//	}
	
	/** The state prefix */
	private final String statePrefix = "q";
	/** Variable for keeping track of state number - reset on call to compile(String) */
	private int stateIndex = 0;
	/** The current matcher string */
	private String currentMatcher;
	
	public StressPatternCompiler() {
		super();
	}
	
	public SimpleFSA<StressMatcherType> compile(String matcherString) 
		throws ParseException {
		
		stateIndex = 0;
		currentMatcher = matcherString;
		
		Tokenizer tokenizer = getTokenizer();
		TokenizerSource source = new StringSource(matcherString);
		tokenizer.setSource(source);
		
		return tokensToFSA(tokenizer);
	}
	
	private SimpleFSA<StressMatcherType> tokensToFSA(Tokenizer tokenizer) 
		throws ParseException {
		SimpleFSA<StressMatcherType> fsa = new SimpleFSA<StressMatcherType>();
		
		// add initial state
		String initialState = getNextStateName();
		fsa.addState(initialState);
		fsa.setInitialState(initialState);
		
		// test first token
		Token token = null;
		try {
			token = tokenizer.nextToken();
		} catch (TokenizerException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		if(token != null
				&& !token.getImage().equals("#"))
			tokenizer.setReadPositionAbsolute(0);
		
		StressMatcherType currentType = null;
		while((currentType = readMatcher(tokenizer)) != null) {
			newTransition(fsa, currentType);
			
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
	
	private StressMatcherType readMatcher(Tokenizer tokenizer) 
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
			if(token.getCompanion() instanceof StressMatcherType) {
				return (StressMatcherType)token.getCompanion();
			} else {
				throw new ParseException(currentMatcher, tokenizer.getReadPosition());
			}
		} else {
			if(token.getType() == Token.WHITESPACE) {
				return StressMatcherType.WordBoundary;
			} else 
				throw new ParseException(currentMatcher, tokenizer.getReadPosition());
		}
		
	}
	
	/**
	 * Strips the final states from the fsa and returns the
	 * stripped values.
	 * 
	 * @param fsa
	 */
	private String[] stripFinalStates(SimpleFSA<StressMatcherType> fsa) {
		String[] currentFinals = fsa.getFinalStates();
		for(String finalState:currentFinals)
			fsa.removeFinalState(finalState);
		return currentFinals;
	}
	
	/**
	 * Returns the next state name
	 * 
	 */
	private String getNextStateName() {
		return statePrefix + (stateIndex++);
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
	private void newTransition(SimpleFSA<StressMatcherType> fsa, StressMatcherType matcher) {
		// strip final states
		String[] oldFinals = stripFinalStates(fsa);
		
		// create a new state and make it final
		String newState = getNextStateName();
		fsa.addState(newState);
		fsa.addFinalState(newState);
		
		for(String oldFinal:oldFinals) {
			StressMatcherTransition transition = new StressMatcherTransition(matcher);
			transition.setFirstState(oldFinal);
			transition.setToState(newState);
			
			fsa.addTransition(transition);
		}
		
		if(oldFinals.length == 0) {
			StressMatcherTransition	transition = new StressMatcherTransition(matcher);
			transition.setFirstState(fsa.getInitialState());
			transition.setToState(newState);
			
			fsa.addTransition(transition);
		}
	}
	
	private void makeZeroOrOne(SimpleFSA<StressMatcherType> fsa, StressMatcherType matcher) {
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:fsa.getFinalStates()) {
			// get transtions to the final state
			for(FSATransition<StressMatcherType> trans:fsa.getTransitionsToState(finalState)) {
				fsa.addFinalState(trans.getFirstState());
			}
		}
	}

	private void makeZeroOrMore(SimpleFSA<StressMatcherType> fsa, StressMatcherType matcher) {
		// for each final state, find the transitions to it and
		// make the first state final as well
		for(String finalState:fsa.getFinalStates()) {
			// get transtions to the final state
			for(FSATransition<StressMatcherType> trans:fsa.getTransitionsToState(finalState)) {
				fsa.addFinalState(trans.getFirstState());
			}
			
			StressMatcherTransition	transition = new StressMatcherTransition(matcher);
			transition.setFirstState(finalState);
			transition.setToState(finalState);
			fsa.addTransition(transition);
		}
	}
	
	private void makeOneOrMore(SimpleFSA<StressMatcherType> fsa, StressMatcherType matcher) {
		// make a new state
		String newState = getNextStateName();
		fsa.addState(newState);
		
		StressMatcherTransition	transition = new StressMatcherTransition(matcher);
		transition.setFirstState(newState);
		transition.setToState(newState);
		fsa.addTransition(transition);
		
		for(String finalState:fsa.getFinalStates()) {
			transition = null;
			
			transition = new StressMatcherTransition(matcher);
			transition.setFirstState(finalState);
			transition.setToState(newState);
			fsa.addTransition(transition);
		}
		
		fsa.addFinalState(newState);
	}
	
	private void makeHashAtEnd(SimpleFSA<StressMatcherType> fsa) {
		// add the anything matcher to all final states
		String newState = getNextStateName();
		
//		FeatureSetMatcher fsm = new FeatureSetMatcher();
		
		fsa.addState(newState);
		
		for(String finalState:fsa.getFinalStates()) {
			StressMatcherTransition smt = new StressMatcherTransition(StressMatcherType.DontCare);
			smt.setFirstState(finalState);
			smt.setToState(newState);
			fsa.addTransition(smt);
		}
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
		for(StressMatcherType stType:StressMatcherType.values()) {
			props.addSpecialSequence(""+stType.getImage(), stType);
		}
		
		// add quantifiers
		for(Quantifier q:Quantifier.values()) {
			props.addSpecialSequence(""+q.getImage(), q);
		}
		
		return new StandardTokenizer(props);
	}
}
