package ca.phon.phonex.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	 * 
	 * @param diacriticList
	 */
	protected DiacriticPhoneMatcher(String diacriticList) {
		super();
		parseDiacriticList(diacriticList, allowedDiacritics, forbiddenDiacritics);
	}
	
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
	
	private void parseDiacriticList(String expr, List<Character> allowed, List<Character> forbidden) {
		boolean isForbidden = false;
		for(Character c:expr.toCharArray()) {
			if(!Character.isWhitespace(c)) {
				if(c == '-') {
					isForbidden = true;
				} else {
					if(isForbidden)
						forbidden.add(c);
					else
						allowed.add(c);
				}
			}
		}
	}

}
