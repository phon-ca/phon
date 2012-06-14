package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.IPAElement;

/**
 * Transition for matching word boundaries.
 *
 */
public class WordBoundaryTransition extends PhonexTransition {
	
	/**
	 * match length - 0 if at beginning/end of input, 1 if matching a space
	 * set when calling 'follow' and it matches
	 */
	private int matchLength = 0;
	
	public WordBoundaryTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		boolean retVal = false;
		
		if(currentState.getTapeIndex() == 0 && currentState.getCurrentState().equals("q0") ||
				currentState.getTapeIndex() == currentState.getTape().length) {
			retVal = true;
			matchLength = 0;
		} else {
			IPAElement p = currentState.getTape()[currentState.getTapeIndex()];
			if(p.getText().equals(" ")) {
				retVal = true;
				matchLength = 1;
			}
		}
		
		return retVal;
	}

	@Override
	public int getMatchLength() {
		return matchLength;
	}
	
	@Override
	public String getImage() {
		return "\\b";
	}
	
	@Override
	public Object clone() {
		WordBoundaryTransition retVal = new WordBoundaryTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

}
