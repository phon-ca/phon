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
package ca.phon.phonex;

import ca.phon.ipa.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

/**
 * Handles matching compound phones using two separate
 * phone matchers.
 */
public class CompoundPhoneMatcher implements PhoneMatcher {
	
	private PhoneMatcher p1Matcher;
	
	private PhoneMatcher p2Matcher;
	
	public CompoundPhoneMatcher(PhoneMatcher p1Matcher, PhoneMatcher p2Matcher) {
		this.p1Matcher = p1Matcher;
		this.p2Matcher = p2Matcher;
	}

	@Override
	public boolean matches(IPAElement p) {
		CompoundPhoneVisitor visitor = new CompoundPhoneVisitor();
		p.accept(visitor);
		return visitor.matches();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

	/**
	 * Visitor for compound phones
	 */
	public class CompoundPhoneVisitor extends VisitorAdapter<IPAElement> {
		
		private boolean matches = false;
		
		public boolean matches() {
			return this.matches;
		}

		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			matches = 
					p1Matcher.matches(cp.getFirstPhone())
					&&
					p2Matcher.matches(cp.getSecondPhone());
		}
		
	}
}
