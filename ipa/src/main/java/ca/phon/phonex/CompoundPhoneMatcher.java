package ca.phon.phonex;

import ca.phon.ipa.phone.CompoundPhone;
import ca.phon.ipa.phone.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Handles matching compound phones using two separate
 * phone matchers.
 */
public class CompoundPhoneMatcher implements PhoneMatcher {
	
	private PhoneMatcher p1Matcher;
	
	private PhoneMatcher p2Matcher;
	
	public CompoundPhoneMatcher(PhoneMatcher p1Matcher, PhoneMatcher p2Matcher) {
		this.p1Matcher = p1Matcher;
		this.p2Matcher = p2Matcher;
	}

	@Override
	public boolean matches(Phone p) {
		CompoundPhoneVisitor visitor = new CompoundPhoneVisitor();
		p.accept(visitor);
		return visitor.matches();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

	/**
	 * Visitor for compound phones
	 */
	public class CompoundPhoneVisitor extends VisitorAdapter<Phone> {
		
		private boolean matches = false;
		
		public boolean matches() {
			return this.matches;
		}

		@Override
		public void fallbackVisit(Phone obj) {
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			matches = 
					p1Matcher.matches(cp.getFirstPhone())
					&&
					p2Matcher.matches(cp.getSecondPhone());
		}
		
	}
}
