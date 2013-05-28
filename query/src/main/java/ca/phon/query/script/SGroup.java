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

import ca.phon.session.Record;
import ca.phon.util.Range;

/**
 * A group.  Groups can be searched as a
 * whole, or individual words within the group
 * can be searched.
 * 
 * 
 */
public class SGroup extends SRange implements ScriptGroup {
	
	/** Words */
	protected Range[] words;
	
	/** Constructor */
	public SGroup(Record record, Range r, int recordIndex, String tierName, int gIndex) {
		super(record, r, recordIndex, tierName, gIndex);
		
		updateWords();
	}
	
	protected void updateWords() {
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
	
	/* Access methods */
	/**
	 * Return the number of words in the group.
	 * 
	 * @return the number of words
	 */
	@Override
	public int getNumberOfWords() {
		return words.length;
	}
	
	/**
	 * Return the word at the given index.
	 * 
	 * @param wIndex
	 * @return the word, or <CODE>null</CODE> if the
	 * given index is outside of the word range.
	 */
	@Override
	public ScriptWord getWord(int wIndex) {
		if(wIndex < 0 || wIndex >= getNumberOfWords()) {
//			return null;
			return new SWord(record, new Range(0, 0, false), recordIndex, tierName, gIndex);
		}
		
		return new SWord(record, words[wIndex], recordIndex, tierName, gIndex);
	}
}
