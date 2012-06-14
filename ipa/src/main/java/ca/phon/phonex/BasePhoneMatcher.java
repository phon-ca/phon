package ca.phon.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.elements.Phone;
import ca.phon.ipa.elements.SyllableBoundary;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Basic, single-character matcher in Phonex.
 * This will NOT matcher compound phones - only
 * {@link Phone} object whose base character
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
	public boolean matches(IPAElement p) {
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
	public class BasePhoneVisitor extends VisitorAdapter<IPAElement> {
		
		private boolean matches = false;
		
		public boolean matches() {
			return this.matches;
		}
		
		@Visits
		public void visitBasicPhone(Phone bp) {
			Character phoneBp = bp.getBasePhone();
			matches = phoneBp == baseChar;
		}
		
		@Visits
		public void visitSyllableBoundary(SyllableBoundary sb) {
			matches = sb.toString().equals(
					BasePhoneMatcher.this.toString());
		}

		@Override
		public void fallbackVisit(IPAElement obj) {
		}
	}
	
	@Override
	public String toString() {
		return this.baseChar + "";
	}

}
