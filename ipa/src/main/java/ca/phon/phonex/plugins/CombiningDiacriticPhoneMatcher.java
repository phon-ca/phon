/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.phonex.plugins;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElement;
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
