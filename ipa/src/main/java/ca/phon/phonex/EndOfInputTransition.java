package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.IPAElement;

/**
 * Transition for matching end of tape input.
 */
public class EndOfInputTransition extends PhonexTransition {
	
	public EndOfInputTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		return currentState.getTapeIndex() == currentState.getTape().length;
	}

	@Override
	public int getMatchLength() {
		return 0;
	}
	
	@Override
	public String getImage() {
		return "$";
	}
	
	@Override
	public Object clone() {
		EndOfInputTransition retVal = new EndOfInputTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

}
