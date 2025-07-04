/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.ipa.*;
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
	public class PrefixDiacriticVisitor extends VisitorAdapter<IPAElement> {
		
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
