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


package ca.phon.syllabifier.basic;

import ca.phon.ipa.IPAElement;
import ca.phon.syllabifier.basic.io.SonorityValues;
import ca.phon.syllabifier.basic.io.SonorityValues.SonorityClass;

/**
 * @author Greg
 *
 */
public class SonorityScale {

	private final SonorityValues values;
	
	SonorityScale(SonorityValues values) {
		super();
		this.values = values;
	}
	
	/**
	 * Calculate the sonority distance between 
	 * two syllabification components.
	 * 
	 * @param comp1
	 * @param comp2
	 * @return int positive if rising sonority, negative is falling sonority
	 */
	public int calculateSonorityDistance(IPAElement comp1, IPAElement comp2) {
		// from our list of rules, try to
		// get a match on the correct sonority value
		int comp1SonorityValue = getSonorityValue(comp1);
		int comp2SonorityValue = getSonorityValue(comp2);
		
		return comp1SonorityValue - comp2SonorityValue;
	}
	
	private int getSonorityValue(IPAElement phone) {
		// rules are checked in the order they are specified in
		// the xml description.  The value from the first matched
		// rule is used
		for(SonorityClass sonorityDef:values.getSonorityClass()) {
			final Constraint currentConstraint = 
				new Constraint(sonorityDef.getConstraint());
			if(currentConstraint.matchesPhone(phone)) {
				return sonorityDef.getSonorityValue();
			}
		}
		return 0;
	}

}
