/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.phonex.plugins;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
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
		public void visitDiacritic(Diacritic dia) {
			matches |= getMatcher().matches(dia);
		}

		@Visits
		public void visitPhone(Phone p) {
			for(Diacritic dia:p.getToneDiacritics()) {
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
