package ca.phon.phonex;

import ca.phon.ipa.phone.Phone;

/**
 * Interface for providing 'matching' capabilites
 * for {@link Phone} objects.
 */
public interface PhoneMatcher {

	/**
	 * Perform test on given phone.
	 * 
	 * @param phone 
	 * @returns <code>true</code> if <code>phone</code>
	 *  matches, <code>false</code> otherwise
	 */
	public boolean matches(Phone p);
	
	/**
	 * Does this matcher match anything? 
	 * Used at runtime to help determine
	 * proper backtracking paths.
	 * 
	 * @returns <code>true</code> if this matcher
	 *  matches anything, <code>false</code> otherwise
	 *  
	 */
	public boolean matchesAnything();
	
}
