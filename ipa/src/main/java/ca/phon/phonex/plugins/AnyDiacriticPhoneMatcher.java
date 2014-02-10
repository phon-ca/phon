package ca.phon.phonex.plugins;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPAElementFactory;
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
		public void visitBasicPhone(Phone phone) {
			final IPAElementFactory factory = new IPAElementFactory();
			
			// prefix
			if(phone.getPrefixDiacritic() != null) {
				final Diacritic prefixDiacritic = factory.createDiacritic(phone.getPrefixDiacritic());
				matches |= getMatcher().matches(prefixDiacritic);
			}
			
			if(matches) return;
			
			// suffix
			if(phone.getSuffixDiacritic() != null) {
				final Diacritic suffixDiacritic = factory.createDiacritic(phone.getSuffixDiacritic());
				matches |= getMatcher().matches(suffixDiacritic);
			}
			
			if(matches) return;
			
			// combining
			for(Character diacritic:phone.getCombiningDiacritics()) {
				final Diacritic cmbDiacritic = factory.createDiacritic(diacritic);
				matches |= getMatcher().matches(cmbDiacritic);
				if(matches) return;
			}
			
			// tone
			for(Character diacritic:phone.getToneDiacritics()) {
				final Diacritic toneDiacritic = factory.createDiacritic(diacritic);
				matches |= getMatcher().matches(toneDiacritic);
				if(matches) return;
			}
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone compoundPhone) {
			visit(compoundPhone.getFirstPhone());
			visit(compoundPhone.getSecondPhone());
		}
		
	}
}
