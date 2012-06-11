package ca.phon.phonex.plugins;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.BasicPhone;
import ca.phon.ipa.phone.CompoundPhone;
import ca.phon.ipa.phone.Phone;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Matches combining diacritic portion of the phone.
 * Matches if <em>any</em> of the specified diacritics
 * appear in the {@link Phone}s combining diacritics.
 * 
 */
public class CombiningDiacriticPhoneMatcher extends DiacriticPhoneMatcher {
	
	/**
	 * Constructor
	 */
	public CombiningDiacriticPhoneMatcher(List<Character> allowed) {
		super(allowed, new ArrayList<Character>());
	}
	
	public CombiningDiacriticPhoneMatcher(List<Character> allowed, List<Character> forbidden) {
		super(allowed, forbidden);
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
			boolean hasAllowed = false;
			for(Character c:bp.getCombiningDiacritics()) {
				hasAllowed |= getAllowedDiacritics().contains(c);
			}
			boolean hasForbidden = false;
			for(Character c:bp.getCombiningDiacritics()) {
				hasForbidden |= getForbiddenDiacritics().contains(c);
			}
			matches = hasAllowed && !hasForbidden;
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			visit(cp.getFirstPhone());
		}
		
	}
	
}
