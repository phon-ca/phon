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
 * Basic, single-character matcher in Phonex.
 * This will NOT matcher compound phones - only
 * {@link Phone} object whose base character
 * is the same as the specified matcher character.
 */
public class BasePhoneMatcher implements PhoneMatcher {
	
	/**
	 * Base phone character
	 */
	private Character baseChar;
	
	public BasePhoneMatcher(Character c) {
		this.baseChar = c;
	}

	@Override
	public boolean matches(IPAElement p) {
		BasePhoneVisitor visitor = new BasePhoneVisitor();
		p.accept(visitor);
		return visitor.matches();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}
	
	/**
	 * Visitor for the phone object
	 * 
	 */
	public class BasePhoneVisitor extends VisitorAdapter<IPAElement> {
		
		private boolean matches = false;
		
		public boolean matches() {
			return this.matches;
		}
		
		@Visits
		public void visitStressMarker(StressMarker marker) {
			Character markerChar = marker.getType().getGlyph();
			matches = markerChar.equals(baseChar);
		}
		
		@Visits
		public void visitBasicPhone(Phone bp) {
			Character phoneBp = bp.getBasePhone();
			matches = (phoneBp.equals(baseChar));
		}
		
		@Visits
		public void visitSyllableBoundary(SyllableBoundary sb) {
			matches = sb.toString().equals(
					BasePhoneMatcher.this.toString());
		}
		
		@Visits
		public void visitDiacritic(Diacritic diacritic) {
			matches = diacritic.toString().equals(
					BasePhoneMatcher.this.toString());
		}
		
		@Visits
		public void visitIntraWordPause(IntraWordPause intraWordPause) {
			matches = baseChar.equals(IntraWordPause.INTRA_WORD_PAUSE_CHAR);
		}

		@Visits
		public void visitAlignmentMarker(AlignmentMarker marker) {
			matches = baseChar.equals(AlignmentMarker.ALIGNMENT_CHAR);
		}
		
		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
	}
	
	@Override
	public String toString() {
		return this.baseChar + "";
	}

}
