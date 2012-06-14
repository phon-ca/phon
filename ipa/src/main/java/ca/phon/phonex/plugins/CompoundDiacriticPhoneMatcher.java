package ca.phon.phonex.plugins;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.elements.CompoundPhone;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Handles diacritic matching for compound phones.
 */
public class CompoundDiacriticPhoneMatcher implements PhoneMatcher {

	/**
	 * P1 matcher
	 */
	private final PhoneMatcher p1Matcher;
	
	/**
	 * P2 matcher
	 */
	private final PhoneMatcher p2Matcher;
	
	public CompoundDiacriticPhoneMatcher(PhoneMatcher p1Matcher, PhoneMatcher p2Matcher) {
		this.p1Matcher = p1Matcher;
		this.p2Matcher = p2Matcher;
	}
	
	@Override
	public boolean matches(IPAElement p) {
		final MatcherVisiter visitor = new MatcherVisiter();
		p.accept(visitor);
		return visitor.matches;
	}

	@Override
	public boolean matchesAnything() {
		return p1Matcher == null && p2Matcher == null;
	}

	public class MatcherVisiter extends VisitorAdapter<IPAElement> {
		
		private boolean matches = false;
		
		@Override
		public void fallbackVisit(IPAElement p) {}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			boolean p1Matches = (p1Matcher == null ? true : p1Matcher.matches(cp.getFirstPhone()));
			boolean p2Matches = (p2Matcher == null ? true : p2Matcher.matches(cp.getSecondPhone()));
			matches = p1Matches && p2Matches;
		}
	}
}
