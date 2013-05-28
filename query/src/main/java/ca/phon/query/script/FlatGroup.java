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

import java.util.ArrayList;
import java.util.List;

import ca.phon.application.transcript.IUtterance;
import ca.phon.util.Range;

public class FlatGroup extends SRange implements ScriptGroup {

	protected Range[] words;
	
	public FlatGroup(IUtterance utt, Range r, int uttIndex, String tier) {
		super(utt, r, uttIndex, tier);
		updateWords();
	}
	
	private void updateWords() {
		String tierValue = 
			super.getData();
		
		List<Range> ranges = new ArrayList<Range>();
		Range currentRange = new Range(0, 0);
		for(int i = 0; i < tierValue.length(); i++) {
			char c = tierValue.charAt(i);
			
			if(Character.isWhitespace(c)) {
				currentRange.setEnd(i);
				currentRange.setExcludesEnd(true);
				ranges.add(currentRange);
				currentRange = new Range(i+1, i+1);
			}
			
			if(i == tierValue.length()-1) {
				currentRange.setEnd(i+1);
				ranges.add(currentRange);
			}
		}
		
		words = ranges.toArray(new Range[0]);
	}

	@Override
	public int getNumberOfWords() {
		return words.length;
	}

	@Override
	public ScriptWord getWord(int wIndex) {
		if(wIndex < 0 || wIndex >= getNumberOfWords())
			return null;
		
		return new SWord(utt, words[wIndex], uttIndex, tierName, gIndex);
	}

}
