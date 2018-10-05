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
