package ca.phon.phonex;

import ca.phon.ipa.AlignmentMarker;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IntraWordPause;
import ca.phon.ipa.Phone;
import ca.phon.ipa.SyllableBoundary;
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
			matches = (phoneBp.equals(baseChar));
		}
		
		@Visits
		public void visitSyllableBoundary(SyllableBoundary sb) {
			matches = sb.toString().equals(
					BasePhoneMatcher.this.toString());
		}
		
		@Visits
		public void visitDiacritic(Diacritic diacritic) {
			matches = diacritic.toString().equals(
					BasePhoneMatcher.this.toString());
		}
		
		@Visits
		public void visitIntraWordPause(IntraWordPause intraWordPause) {
			matches = baseChar.equals(IntraWordPause.INTRA_WORD_PAUSE_CHAR);
		}

		@Visits
		public void visitAlignmentMarker(AlignmentMarker marker) {
			matches = baseChar.equals(AlignmentMarker.ALIGNMENT_CHAR);
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
