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

import java.util.List;

import ca.phon.application.transcript.Form;
import ca.phon.application.transcript.IPhoneticRep;
import ca.phon.application.transcript.IUtterance;
import ca.phon.featureset.FeatureSet;
import ca.phon.gui.recordeditor.SystemTierType;
import ca.phon.phone.Phone;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Range;

public class SIPAPhone extends SIPARange {

	public SIPAPhone(IUtterance utt, Range r, int uttIndex, String tier,
			int index) {
		super(utt, r, uttIndex, tier, index);
	}
	
	public FeatureSet getFeatureSet() {
		Phone p = getPhone();
		return p.getFeatureSet();
	}
	
	public SyllableConstituentType getScType() {
		return getPhone().getScType();
	}
	
	public Phone getPhone() {
		// get the phone
		Form form =
			(SystemTierType.IPATarget.getTierName().equals(tierName) ? Form.Target : Form.Actual);
		
		IPhoneticRep phoRep = 
			utt.getWords().get(gIndex).getPhoneticRepresentation(form);
		
		Phone retVal = new Phone("");
//		FeatureSet retVal = new FeatureSet();
		if(phoRep != null) {
			List<Phone> phones = phoRep.getPhones();
			
			Range phoneRange = 
				Phone.convertStringRangeToPhoneRange(getData(), range);
			
			if(phoneRange.getFirst() >= 0 && phoneRange.getFirst() < phones.size()) {
				retVal = phones.get(phoneRange.getFirst());
			}
		}
		return retVal;
	}

}
