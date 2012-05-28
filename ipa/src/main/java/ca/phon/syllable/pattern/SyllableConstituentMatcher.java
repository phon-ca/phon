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
package ca.phon.syllable.pattern;

import java.util.ArrayList;
import java.util.logging.Logger;

import ca.phon.phone.Phone;
import ca.phon.phone.PhoneSequenceMatcher;
import ca.phon.syllable.Syllable;


/**
 * 
 *
 */
public class SyllableConstituentMatcher implements SyllableMatcher {
	
	private ArrayList<ArrayList<String>> matchedOptionalFeatures;
	private ArrayList<ArrayList<String>> replacementTable;

	private static class MatcherTuple {
		String identifier = "";
		PhoneSequenceMatcher matcher;
	}
	
	/** The list of identifiers and their phone matchers */
	private ArrayList<MatcherTuple> matchers;
	
	/** Constructor */
	public SyllableConstituentMatcher() {
		super();
		
		this.matchers = new ArrayList<MatcherTuple>();
		
		this.matchedOptionalFeatures = new ArrayList<ArrayList<String>>();
		this.replacementTable = new ArrayList<ArrayList<String>>();
	}
	
	/**
	 * Add a matcher
	 * 
	 */
	public void addMatcher(String identifier, PhoneSequenceMatcher matcher) {
		MatcherTuple mt = new MatcherTuple();
		mt.identifier = identifier;
		mt.matcher = matcher;
		
		this.matchers.add(mt);
	}
	
	@Override
	public boolean matches(Syllable syllable) {
		boolean matches = true;
		
		this.matchedOptionalFeatures.clear();
	
		for(MatcherTuple mt:matchers) {
			PhoneSequenceMatcher matcher = mt.matcher;
			
			matcher.setReplacementTable(replacementTable);
			
			if(syllable == null) {
				matches = false; 
				break;
			}
			
			if(mt == null) {
				Logger.getLogger(getClass().getName()).warning("Illegal matcher");
			}
			
			ArrayList<Phone> ps = syllable.getPhonesForIdentifier(mt.identifier);
			
			if(!matcher.follows(ps)) {
				matches = false;
				break;
			}
			
			// add the items to the replacement table
			this.matchedOptionalFeatures.addAll(matcher.getMatchedOptionalFeatures());
		}
		
		return matches;
	}

	@Override
	public ArrayList<ArrayList<String>> getMatchedOptionalFeatures() {
		return matchedOptionalFeatures;
	}

	@Override
	public void setReplacementTable(ArrayList<ArrayList<String>> matchedOptionalFeatures) {
		this.replacementTable = matchedOptionalFeatures;
	}

}
