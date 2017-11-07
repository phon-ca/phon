package ca.phon.phonex;

import ca.phon.ipa.*;

public class IntraWordPauseMatcher implements PhoneMatcher {

	public IntraWordPauseMatcher() {
	}

	@Override
	public boolean matches(IPAElement p) {
		return p instanceof IntraWordPause;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

}
