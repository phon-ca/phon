package ca.phon.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.FSATransition;
import ca.phon.fsa.TransitionType;
import ca.phon.ipa.IPAElement;

/**
 * Transitions using {@link PhoneMatcher}s.
 * 
 */
public class PhonexTransition extends FSATransition<IPAElement> implements Cloneable {
	
	/**
	 * Base matcher
	 */
	private PhoneMatcher baseMatcher;
	
	/**
	 * Syllabification matcher
	 */
	private List<PhoneMatcher> secondaryMatchers = 
			new ArrayList<PhoneMatcher>();
	
	/**
	 * Create a new transition with
	 * the given base phone matcher
	 *
	 * @param matcher
	 */
	public PhonexTransition(PhoneMatcher matcher) {
		this(matcher, new PhoneMatcher[0]);
	}
	
	/**
	 * Create a new transitions with the given
	 * base matcher and secondary matchers
	 * 
	 * @param matcher
	 * @param secondaryMatchers
	 */
	public PhonexTransition(PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		this.baseMatcher = matcher;
		if(secondaryMatchers != null) {
			for(PhoneMatcher secondaryMatcher:secondaryMatchers) {
				if(secondaryMatcher != null)
					this.secondaryMatchers.add(secondaryMatcher);
			}
		}
	}
	
	/**
	 * Get the base phone matcher
	 * 
	 * @return the base phone matcher
	 */
	public PhoneMatcher getMatcher() {
		return this.baseMatcher;
	}
	
	/**
	 * Get the secondary matchers
	 * 
	 * @return the list secondary {@link PhoneMatcher} matcher
	 */
	public List<PhoneMatcher> getSecondaryMatchers() {
		return this.secondaryMatchers;
	}
	
	/**
	 * Set the type matcher
	 * 
	 * @param typeMatcher
	 */
	public void addSecondaryMatcher(PhoneMatcher typeMatcher) {
		this.secondaryMatchers.add(typeMatcher);
	}
	
	/**
	 * Remove secondary matcher
	 * 
	 * @param matcher
	 */
	public void removeSecondaryMatcher(PhoneMatcher matcher) {
		this.secondaryMatchers.remove(matcher);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		if(currentState.getTapeIndex() >= 
				currentState.getTape().length)
			return false;
		IPAElement obj = currentState.getTape()[currentState.getTapeIndex()];
		boolean retVal = 
				baseMatcher.matches(obj);
		
		for(PhoneMatcher matcher:secondaryMatchers) {
			retVal &= matcher.matches(obj);
		}
		
		return retVal;
	}

	@Override
	public String getImage() {
		String retVal = baseMatcher.toString();
		for(PhoneMatcher pm:secondaryMatchers) {
			retVal += ":" + pm.toString();
		}
		if(getType() != TransitionType.NORMAL) {
			retVal += " (" + getType() + ")";
		}
		return retVal;
	}
	
	@Override
	public Object clone() {
		PhonexTransition retVal = new PhonexTransition(baseMatcher, secondaryMatchers.toArray(new PhoneMatcher[0]));
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		retVal.getInitGroups().addAll(getInitGroups());
		retVal.getMatcherGroups().addAll(getMatcherGroups());
		return retVal;
	}
}
