/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.syllabifier;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ca.phon.exceptions.ParserException;
import ca.phon.syllabifier.io.PhoneSequenceConstraint;
import ca.phon.util.Range;

public class PhoneSequenceRule extends PhoneSequenceConstraint implements
		SyllabificationRule {

	@Override
	public boolean checkRule(Phone comp) {
		try {
			PhoneSequenceMatcher matcher = 
				PhoneSequenceMatcher.compile(getValue());
			
			List<Phone> testList = new ArrayList<Phone>();
			testList.add(comp);
			return matcher.follows(testList);
		} catch (ParserException e) {
			Logger.getLogger(getClass().getName()).warning(e.getErrMsg(false));
			return false;
		}
	}
	
	@Override
	public boolean matchesEmptyList() {
		try {
			PhoneSequenceMatcher matcher = 
				PhoneSequenceMatcher.compile(getValue());
			
			return matcher.follows(new ArrayList<Phone>());
		} catch (ParserException e) {
			Logger.getLogger(getClass().getName()).warning(e.getErrMsg(false));
		}
		return false;
	}

	@Override
	public List<Range> findRangesInList(List<Phone> tape) {
		try {
			PhoneSequenceMatcher matcher = 
				PhoneSequenceMatcher.compile(getValue());
			
			return matcher.findRanges(tape);
		} catch (ParserException e) {
			Logger.getLogger(getClass().getName()).warning(e.getErrMsg(false));
			return new ArrayList<Range>();
		}
	}

}
