package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.IPAElement;

public class EmptyTransition extends PhonexTransition {

	public EmptyTransition() {
		super(null);
	}

	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		// only follow if we are not at end of tape
		return (currentState.getTapeIndex() < currentState.getTape().length);
	}

	@Override
	public String getImage() {
		return "\u03b5";
	}

	@Override
	public int getMatchLength() {
		return 0;
	}
	
	@Override
	public Object clone() {
		EmptyTransition retVal = new EmptyTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		retVal.getInitGroups().addAll(getInitGroups());
		retVal.getMatcherGroups().addAll(getMatcherGroups());
		retVal.setType(getType());
		retVal.setOffsetType(getOffsetType());
		return retVal;
	}

}
