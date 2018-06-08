package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.IPAElement;

public class EmptyTransition extends PhonexTransition {

	public EmptyTransition() {
		super(null);
	}

	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		return true;
	}

	@Override
	public String getImage() {
		return "\u03b5";
	}

	@Override
	public int getMatchLength() {
		return 0;
	}

}
