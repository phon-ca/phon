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
package ca.phon.query.script;

import ca.phon.application.transcript.IUtterance;
import ca.phon.util.Range;

public class SSyllable extends SIPARange {

	public SSyllable(IUtterance utt, Range range, int uttIndex, String tierName, int gIndex) {
		super(utt, range, uttIndex, tierName, gIndex);	
	}

	/**
	 * Returns the stress of the syllable.
	 *  - 1 = primary
	 *  - 2 = secondary
	 *  - 0 = unstressed
	 */
	public int getStress() {
		int retVal = 0;
		
		if(super.getDataRange().getRange() > 0) {
			// check the first phone in the syllable
			SIPARange sPhone = super.getPhone(0);
			
			if(sPhone.matchesPhonex("{}:PrimaryStress"))
				retVal = 1;
			else if(sPhone.matchesPhonex("{}:SecondaryStress"))
				retVal = 2;
		}
		
		return retVal;
	}
}
