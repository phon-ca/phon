package ca.phon.phonex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipa.phone.Phone;

/**
 * Match phone string against a regular expression.
 * 
 */
public class RegexMatcher implements PhoneMatcher {
	
	/**
	 * Pattern
	 */
	private Pattern pattern;
	
	/**
	 * Create a new matcher for the
	 * given regular expression.
	 * 
	 * @param regex
	 */
	public RegexMatcher(String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean matches(Phone p) {
		Matcher m = pattern.matcher(p.getText());
		return m.matches();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}
	
	@Override
	public String toString() {
		return pattern.pattern();
	}

}
