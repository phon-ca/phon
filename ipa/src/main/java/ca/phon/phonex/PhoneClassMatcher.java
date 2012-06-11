package ca.phon.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.Phone;

/**
 * Combine multiple phone matchers.
 *
 */
public class PhoneClassMatcher implements PhoneMatcher {
	
	/**
	 * List of matchers
	 */
	private List<PhoneMatcher> matchers =
			new ArrayList<PhoneMatcher>();
	
	private boolean not = false;

	public PhoneClassMatcher() {
		this(false, new PhoneMatcher[0]);
	}
	
	/**
	 * Constructor
	 */
	public PhoneClassMatcher(PhoneMatcher ... matchers) {
		this(false, matchers);
	}
	
	/**
	 * Constructor
	 * 
	 * @param ... list of matchers
	 */
	public PhoneClassMatcher(boolean not, PhoneMatcher ... matchers) {
		for(PhoneMatcher matcher:matchers) {
			this.matchers.add(matcher);
		}
	}

	public void addMatcher(PhoneMatcher pm) {
		this.matchers.add(pm);
	}
	
	@Override
	public boolean matches(Phone p) {
		boolean retVal = not;
		
		for(PhoneMatcher matcher:matchers) {
			if(not)
				retVal &= !matcher.matches(p);
			else
				retVal |= matcher.matches(p);
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

}
