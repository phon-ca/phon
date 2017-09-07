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
package ca.phon.phonex;

import ca.phon.ipa.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

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
