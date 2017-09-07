/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.ipa.*;
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
		public void visitDiacritic(Diacritic dia) {
			matches |= getMatcher().matches(dia);
		}
		
		@Visits
		public void visitBasicPhone(Phone phone) {
			// combining
			for(Diacritic dia:phone.getCombiningDiacritics()) {
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
