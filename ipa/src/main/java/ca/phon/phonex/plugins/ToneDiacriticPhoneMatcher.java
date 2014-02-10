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
 * Tests tone diacritics.
 */
public class ToneDiacriticPhoneMatcher extends DiacriticPhoneMatcher {

	public ToneDiacriticPhoneMatcher(String input) {
		super(input);
	}
	
	public ToneDiacriticPhoneMatcher(PhoneMatcher matcher) {
		super(matcher);
	}

	@Override
	public boolean matches(IPAElement p) {
		final ToneDiacriticVisitor visitor = new ToneDiacriticVisitor();
		visitor.visit(p);
		return visitor.matches;
	}

	@Override
	public boolean matchesAnything() {
		return getMatcher().matchesAnything();
	}

	private class ToneDiacriticVisitor extends VisitorAdapter<IPAElement> {
		
		private boolean matches = false;

		@Override
		public void fallbackVisit(IPAElement obj) {
		}

		@Visits
		public void visitPhone(Phone p) {
			final IPAElementFactory factory = new IPAElementFactory();
			final PhoneMatcher pm = getMatcher();
			for(Character c:p.getToneDiacritics()) {
				final Diacritic diacritic = factory.createDiacritic(c);
				matches |= pm.matches(diacritic);
			}
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			visit(cp.getFirstPhone());
			visit(cp.getSecondPhone());
		}
	}
	
}
