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
package ca.phon.ipa.alignment;

import java.util.*;

import ca.phon.alignment.AlignmentMap;
import ca.phon.ipa.*;
import ca.phon.util.Tuple;

/**
 *
 */
public class SyllableMap extends AlignmentMap<IPATranscript> {
	
	/** The target phonetic rep */
	private IPATranscript targetRep;
	/** The actual phonetic rep */
	private IPATranscript actualRep;
	
	private PhoneMap phoneAlignment;
	
	/**
	 * Constructor
	 */
	public SyllableMap(IPATranscript targetRep, IPATranscript actualRep, PhoneMap phoneMap) {
		super();
		
		setTargetRep(targetRep);
		setActualRep(actualRep);
		setPhoneAlignment(phoneMap);
	}
	
	public void setPhoneAlignment(PhoneMap phoneMap) {
		this.phoneAlignment = phoneMap;
		calculateAlignment();
	}
	
	public PhoneMap getPhoneAlignment() {
		return this.phoneAlignment;
	}
	
	protected void calculateAlignment() {
		final IPATranscript target = getTargetRep();
		final IPATranscript actual = getActualRep();
		final PhoneMap align = getPhoneAlignment();
		
		final List<IPATranscript> targetSylls = target.syllables();
		final List<IPATranscript> actualSylls = actual.syllables();
		
		List<Tuple<IPATranscript, IPATranscript>> syllMap = new ArrayList<>();
		for(IPATranscript targetSyll:targetSylls) {
			syllMap.add(new Tuple<>(targetSyll, null));
		}
		
		for(int i = actualSylls.size()-1; i >=0 ; i--) {
			final IPATranscript actualSyll = actualSylls.get(i);
			int eleIdx = actualSyll.indexOf(".:N");
			if(eleIdx < 0)
				eleIdx = actualSyll.indexOf("\\v");
			if(eleIdx < 0)
				eleIdx = actualSyll.indexOf("\\w");
			
			if(eleIdx < 0)
				throw new IllegalArgumentException(actualSyll.toString());
			
			final IPATranscript ele = new IPATranscript(actualSyll.elementAt(eleIdx));
			final List<IPAElement> aligned = align.getAligned(ele);
			
			if(aligned != null && aligned.size() > 0) {
				final IPAElement targetEle = aligned.get(0);
				Tuple<IPATranscript, IPATranscript> mapping = null;
				for(int j = syllMap.size()-1; j >= 0; j--) {
					final Tuple<IPATranscript, IPATranscript> currentMapping = syllMap.get(j);
					if(currentMapping.getObj1() != null && currentMapping.getObj1().indexOf(targetEle) >= 0) {
						mapping = currentMapping;
						break;
					}
				}
				if(mapping != null) {
					mapping.setObj2(actualSyll);
				} else {
					throw new IllegalStateException("Target syllable containing " + targetEle + " not found");
				}
			} else {
				// insert a new mapping at the current syllable index
				final Tuple<IPATranscript, IPATranscript> epen = new Tuple<>(null, actualSyll);
				syllMap.add(i, epen);
			}
		}
		
		// create the alignment arrays
		final int alignLen = syllMap.size();
		Integer alignment[][] = new Integer[2][];
		alignment[0] = new Integer[alignLen];
		alignment[1] = new Integer[alignLen];
		for(int i = 0; i < alignLen; i++) {
			final Tuple<IPATranscript, IPATranscript> mapping = syllMap.get(i);
			
			int targetIdx = (mapping.getObj1() != null ? targetSylls.indexOf(mapping.getObj1()) : -1);
			alignment[0][i] = targetIdx;
			
			int actualIdx = (mapping.getObj2() != null ? actualSylls.indexOf(mapping.getObj2()) : -1);
			alignment[1][i] = actualIdx;
		}
		setTopAlignment(alignment[0]);
		setBottomAlignment(alignment[1]);
	}

	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;
		this.bottomElements = actualRep.syllables().toArray(new IPATranscript[0]);
	}

	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;
		this.topElements = targetRep.syllables().toArray(new IPATranscript[0]);
	}
	
}
