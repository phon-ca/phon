package ca.phon.ipa.phone.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.phone.Phone;

/**
 * Transition for matching end of tape input.
 */
public class EndOfInputTransition extends PhonexTransition {
	
	public EndOfInputTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<Phone> currentState) {
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
