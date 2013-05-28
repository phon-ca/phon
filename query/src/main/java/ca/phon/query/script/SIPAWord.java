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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.phon.application.transcript.IUtterance;
import ca.phon.engines.syllabifier.Syllabifier;
import ca.phon.syllable.Syllable;
import ca.phon.util.Range;

public class SIPAWord extends SIPARange implements ScriptWord {
	
	/** Syllables */
	private Map<Syllable, Range> syllables = new LinkedHashMap<Syllable, Range>();
	
	public SIPAWord(IUtterance utt, Range range, int uttIndex, String tierName, int gIndex) {
		super(utt, range, uttIndex, tierName, gIndex);
		
		updateSyllables();
	}
	
	private void updateSyllables() {
		this.syllables.clear();
		
		List<Syllable> sylls = Syllabifier.getSyllabification(getPhones());
		int currentStart = range.getFirst();
//		int offset = range.getFirst();
		for(Syllable syll:sylls) {
			int currentEnd = currentStart + syll.toString().length();
			syllables.put(syll, new Range(currentStart, currentEnd, true));
			
			currentStart = currentEnd;
		}
	}
	
	public int getNumberOfSyllables() {
		return syllables.size();
	}
	
	public SSyllable getSyllable(int sIndex) {
		if(sIndex < 0 || sIndex >= this.syllables.size()) {
//			return null;
			return new SSyllable(utt, new Range(0, 0, false), uttIndex, tierName, gIndex);
		}
		
		Syllable retSyll = null;
		int syllIndex = 0;
		for(Syllable s:syllables.keySet()) {
			if(syllIndex == sIndex) {
				retSyll = s;
				break;
			}
			syllIndex++;
		}
		
		return new SSyllable(utt, syllables.get(retSyll), uttIndex, tierName, gIndex);
	}
	
}
