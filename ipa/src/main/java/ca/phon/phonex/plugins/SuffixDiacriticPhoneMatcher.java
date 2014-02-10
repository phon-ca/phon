package ca.phon.phonex.plugins;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;

/**
 * 
 */
public class SuffixDiacriticPhoneMatcher extends DiacriticPhoneMatcher {

	

	public SuffixDiacriticPhoneMatcher(String phonex) {
		super(phonex);
	}
	
	public SuffixDiacriticPhoneMatcher(PhoneMatcher matcher) {
		super(matcher);
	}

	@Override
	public boolean matches(IPAElement p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean matchesAnything() {
		return getMatcher().matchesAnything();
	}

}
