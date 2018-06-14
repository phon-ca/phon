package ca.phon.phonex.plugins;

import ca.phon.phonex.PhoneMatcher;

/**
 * Interface for plug-in matchers which are able to be combined.
 * This means that if the same plug-in matcher is specified multiple times,
 * they are 'combined' into a single matcher.  This is useful, for example,
 * with the SyllableConstituentMatcher.
 */
public interface CombinableMatcher {

	public void combineMatcher(PhoneMatcher matcher);
	
}
