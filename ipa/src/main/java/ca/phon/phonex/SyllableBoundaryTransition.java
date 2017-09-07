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

import java.util.List;

import ca.phon.fsa.*;
import ca.phon.ipa.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Used to detect syllable boundaries.  Syllable boundaries
 * can be 'hard' (i.e., a '.' or other punctuation) or 'soft'
 * as detected between a (coda, onset) pair.
 *
 */
public class SyllableBoundaryTransition extends PhonexTransition {

	/**
	 * Length of tape matched
	 */
	private int matchLength = 0;

	public SyllableBoundaryTransition() {
		super(null);
	}

	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		boolean retVal = false;

		final IPATranscript transcript = new IPATranscript(currentState.getTape());
		final List<IPATranscript> sylls = transcript.syllables();

		int tapeIdx = -1;
		if(getOffsetType() == OffsetType.NORMAL) {
			if(currentState.getTapeIndex() >= currentState.getTape().length) return true;
			tapeIdx = currentState.getTapeIndex();
		} else if(getOffsetType() == OffsetType.LOOK_BEHIND) {
			tapeIdx = currentState.getTapeIndex() - currentState.getLookBehindOffset();
			if(tapeIdx < 0) return true;
		} else if(getOffsetType() == OffsetType.LOOK_AHEAD) {
			tapeIdx = currentState.getTapeIndex() + currentState.getLookAheadOffset();
			if(tapeIdx >= currentState.getTape().length) return true;
		}

		// edges
		if(tapeIdx == 0 ||
				tapeIdx == currentState.getTape().length) {
			retVal = true;
			matchLength = 0;
		} else {
			final IPAElement p = currentState.getTape()[tapeIdx];
		// punctuation
			final PunctuationTest test = new PunctuationTest();
			p.accept(test);
			retVal = test.isPunct;
			if(retVal) {
				matchLength = 1;
			}

		// implicit syllable edges
			if(!retVal) {
				for(IPATranscript syll:sylls) {
					if(p == syll.elementAt(0) || p == syll.elementAt(syll.length()-1)) {
						retVal = true;
						matchLength = 0;
					}
				}
			}
		}

		return retVal;
	}

	@Override
	public int getMatchLength() {
		return matchLength;
	}

	@Override
	public String getImage() {
		return "\\S";
	}

	@Override
	public Object clone() {
		SyllableBoundaryTransition retVal = new SyllableBoundaryTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

	/**
	 * Class for testing if a phone is punctuation.
	 *
	 */
	public class PunctuationTest extends VisitorAdapter<IPAElement> {

		private boolean isPunct = true;

		@Override
		public void fallbackVisit(IPAElement obj) {
		}

		@Visits
		public void visitPhone(Phone p) {
			isPunct = false;
		}

		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			isPunct = false;
		}

	}
}
