package ca.phon.syllable.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.BasicPhone;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.phonex.PhoneMatcher;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Matches combining diacritic portion of the phone.
 * Matches if <em>any</em> of the specified diacritics
 * appear in the {@link Phone}s combining diacritics.
 * 
 */
public class CombiningDiacriticPhoneMatcher implements PhoneMatcher {

	/**
	 * Allowed diacritics
	 */
	private final List<Character> allowedDiacritics = 
			new ArrayList<Character>();
	
	/**
	 * Constructor
	 */
	public CombiningDiacriticPhoneMatcher(List<Character> allowedDiacritics) {
		this.allowedDiacritics.addAll(allowedDiacritics);
	}
	
	@Override
	public boolean matches(Phone p) {
		final CombiningDiacriticVisitor visitor = new CombiningDiacriticVisitor();
		p.accept(visitor);
		return visitor.matches;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

	/**
	 * Visitor for match
	 */
	public class CombiningDiacriticVisitor extends VisitorAdapter<Phone> {
		
		public boolean matches = false;

		@Override
		public void fallbackVisit(Phone obj) {
			
		}
		
		@Visits
		public void visitBasicPhone(BasicPhone bp) {
			for(Character c:bp.getCombiningDiacritics()) {
				matches |= allowedDiacritics.contains(c);
			}
		}
		
		
	}
	
}
