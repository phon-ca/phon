package ca.phon.phonex.plugins;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Matches prefix diacritic portion of the phone.
 * Matches if <em>any</em> of the specified diacritics
 * appear in the {@link IPAElement}s prefix diacritics.
 */
public class PrefixDiacriticPhoneMatcher extends DiacriticPhoneMatcher {

	/**
	 * Constructor
	 */
	public PrefixDiacriticPhoneMatcher(String input) {
		super(input);
	}
	
	@Override
	public boolean matches(IPAElement p) {
		final PrefixDiacriticVisitor visitor = new PrefixDiacriticVisitor();
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
	private class PrefixDiacriticVisitor extends VisitorAdapter<IPAElement> {
		
		public boolean matches = false;

		@Override
		public void fallbackVisit(IPAElement obj) {
			
		}
		
		@Visits
		public void visitDiacritic(Diacritic dia) {
			matches |= getMatcher().matches(dia);
			if(!matches) {
				for(Diacritic subdia:dia.getPrefixDiacritics()) {
					visit(subdia);
				}
				for(Diacritic subdia:dia.getSuffixDiacritics()) {
					visit(subdia);
				}
			}
		}
		
		@Visits
		public void visitBasicPhone(Phone phone) {
			for(Diacritic dia:phone.getPrefixDiacritics()) {
				visit(dia);
			}
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			visit(cp.getFirstPhone());
			visit(cp.getSecondPhone()); 
		}
		
	}

}
