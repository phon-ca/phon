package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.phone.Phone;

/**
 * Transition for matching beginning of tape input.
 */
public class BeginningOfInputTransition extends PhonexTransition {
	
	public BeginningOfInputTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<Phone> currentState) {
		return currentState.getTapeIndex() == 0;
	}

	@Override
	public int getMatchLength() {
		return 0;
	}
	
	@Override
	public String getImage() {
		return "^";
	}
	
	@Override
	public Object clone() {
		BeginningOfInputTransition retVal = new BeginningOfInputTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

}
