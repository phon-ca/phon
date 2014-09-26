package ca.phon.phonex.plugins;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPAElementFactory;
import ca.phon.ipa.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Matches combining diacritic portion of the phone.
 * Matches if <em>any</em> of the specified diacritics
 * appear in the {@link IPAElement}s combining diacritics.
 */
public class CombiningDiacriticPhoneMatcher extends DiacriticPhoneMatcher {

	/**
	 * Constructor
	 */
	public CombiningDiacriticPhoneMatcher(String input) {
		super(input);
	}
	
	@Override
	public boolean matches(IPAElement p) {
		final CombiningDiacriticVisitor visitor = new CombiningDiacriticVisitor();
		p.accept(visitor);
		return visitor.matches;
	}

	@Override
	public boolean matchesAnything() {
		return getMatcher().matchesAnything();
	}

	/**
	 * Visitor for match
	 */
	private class CombiningDiacriticVisitor extends VisitorAdapter<IPAElement> {
		
		public boolean matches = false;

		@Override
		public void fallbackVisit(IPAElement obj) {
			
		}
		
		@Visits
		public void visitBasicPhone(Phone phone) {
			final IPAElementFactory factory = new IPAElementFactory();
			// combining
			for(Character diacritic:phone.getCombiningDiacritics()) {
				final Diacritic cmbDiacritic = factory.createDiacritic(diacritic);
				matches |= getMatcher().matches(cmbDiacritic);
				if(matches) return;
			}
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			visit(cp.getFirstPhone());
			visit(cp.getSecondPhone()); 
		}
		
	}
	
}
