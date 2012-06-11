package ca.phon.phonex;

import ca.phon.ipa.phone.BasicPhone;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.SyllableBoundary;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Basic, single-character matcher in Phonex.
 * This will NOT matcher compound phones - only
 * {@link BasicPhone} object whose base character
 * is the same as the specified matcher character.
 */
public class BasePhoneMatcher implements PhoneMatcher {
	
	/**
	 * Base phone character
	 */
	private Character baseChar;
	
	public BasePhoneMatcher(Character c) {
		this.baseChar = c;
	}

	@Override
	public boolean matches(Phone p) {
		BasePhoneVisitor visitor = new BasePhoneVisitor();
		p.accept(visitor);
		return visitor.matches();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}
	
	/**
	 * Visitor for the phone object
	 * 
	 */
	public class BasePhoneVisitor extends VisitorAdapter<Phone> {
		
		private boolean matches = false;
		
		public boolean matches() {
			return this.matches;
		}
		
		@Visits
		public void visitBasicPhone(BasicPhone bp) {
			Character phoneBp = bp.getBasePhone();
			matches = phoneBp == baseChar;
		}
		
		@Visits
		public void visitSyllableBoundary(SyllableBoundary sb) {
			matches = sb.toString().equals(
					BasePhoneMatcher.this.toString());
		}

		@Override
		public void fallbackVisit(Phone obj) {
		}
	}
	
	@Override
	public String toString() {
		return this.baseChar + "";
	}

}
