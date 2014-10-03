package ca.phon.phonex.plugins;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Matches if a diacritic in the prefix, suffix, or
 * combining section of the phone is matched by
 * the parent matcher.
 * 
 */
public class AnyDiacriticPhoneMatcher extends DiacriticPhoneMatcher {

	public AnyDiacriticPhoneMatcher(String phonex) {
		super(phonex);
	}
	
	public AnyDiacriticPhoneMatcher(PhoneMatcher matcher) {
		super(matcher);
	}

	@Override
	public boolean matches(IPAElement p) {
		final DiacriticVisitor visitor = new DiacriticVisitor();
		visitor.visit(p);
		return visitor.matches;
	}

	@Override
	public boolean matchesAnything() {
		return getMatcher().matchesAnything();
	}

	public class DiacriticVisitor extends VisitorAdapter<IPAElement> {
		
		private boolean matches = matchesAnything();

		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
		@Visits
		public void visitDiacritic(Diacritic dia) {
			matches |= getMatcher().matches(dia);
			if(!matches) {
				// try sub-diacritics
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
			// prefix
			for(Diacritic dia:phone.getPrefixDiacritics()) {
				visit(dia);
			}
			
			if(matches) return;
			
			// suffix
			for(Diacritic dia:phone.getCombiningDiacritics()) {
				visit(dia);
			}
			
			if(matches) return;
			
			// combining
			for(Diacritic dia:phone.getSuffixDiacritics()) {
				visit(dia);
			}
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone compoundPhone) {
			visit(compoundPhone.getFirstPhone());
			visit(compoundPhone.getSecondPhone());
		}
		
	}
}
