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

package ca.phon.ipa.alignment;

import java.util.List;

import ca.phon.alignment.AlignmentMap;
import ca.phon.ipa.AudiblePhoneVisitor;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;

/**
 * 
 */
public class PhoneMap extends AlignmentMap<IPAElement> {
	/** The target phonetic rep */
	private IPATranscript targetRep;
	/** The actual phonetic rep */
	private IPATranscript actualRep;
	
	/**
	 * Constructor
	 */
	public PhoneMap(IPATranscript targetRep, IPATranscript actualRep) {
		super();
		
		setTargetRep(targetRep);
		setActualRep(actualRep);
	}

	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;
		
		final AudiblePhoneVisitor visitor = new AudiblePhoneVisitor();
		actualRep.accept(visitor);
		
		this.bottomElements = 
			visitor.getPhones().toArray(new IPAElement[0]);
	}

	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;
		
		final AudiblePhoneVisitor visitor = new AudiblePhoneVisitor();
		targetRep.accept(visitor);
		
		this.topElements = 
			visitor.getPhones().toArray(new IPAElement[0]);
	}
	
}
