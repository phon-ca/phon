/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
 * 
 */
public class SuffixDiacriticPhoneMatcher extends DiacriticPhoneMatcher {

	public SuffixDiacriticPhoneMatcher(String phonex) {
		super(phonex);
	}
	
	public SuffixDiacriticPhoneMatcher(PhoneMatcher matcher) {
		super(matcher);
	}

	@Override
	public boolean matches(IPAElement p) {
		final SuffixDiacriticVisitor visitor = new SuffixDiacriticVisitor();
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
	private class SuffixDiacriticVisitor extends VisitorAdapter<IPAElement> {
		
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
			for(Diacritic dia:phone.getSuffixDiacritics()) {
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
