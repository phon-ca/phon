package ca.phon.phonex.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;

/**
 * 
 */
public abstract class DiacriticPhoneMatcher implements PhoneMatcher {
	
	/**
	 * List of allowed diacritics
	 */
	private final List<Character> allowedDiacritics = 
			new ArrayList<Character>();
	
	/**
	 * List of forbidden diacritics
	 */
	public final List<Character> forbiddenDiacritics =
			new ArrayList<Character>();
	
	/**
	 * Constructor
	 */
	protected DiacriticPhoneMatcher(List<Character> allowed, List<Character> forbidden) {
		this.allowedDiacritics.addAll(allowed);
		this.forbiddenDiacritics.addAll(forbidden);
	}
	
	/**
	 * Get allowed diacritics
	 */
	public List<Character> getAllowedDiacritics() {
		return Collections.unmodifiableList(allowedDiacritics);
	}
	
	/**
	 * Get forbidden diacritics
	 */
	public List<Character> getForbiddenDiacritics() {
		return Collections.unmodifiableList(forbiddenDiacritics);
	}

}
