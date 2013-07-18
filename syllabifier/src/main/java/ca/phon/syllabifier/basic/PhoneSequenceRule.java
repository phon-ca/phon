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

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.util.Range;

public class PhoneSequenceRule implements SyllabificationRule {

	private final PhonexPattern pattern;
	
	PhoneSequenceRule(String phonex) {
		super();
		
		this.pattern = PhonexPattern.compile(phonex);
	}
	
	@Override
	public boolean checkRule(IPAElement comp) {
		final IPATranscript t = new IPATranscript(comp);
		return t.matches(pattern.pattern());
	}
	
	@Override
	public boolean matchesEmptyList() {
		final IPATranscript t = new IPATranscript();
		return t.matches(pattern.pattern());
	}

	@Override
	public List<Range> findRangesInList(List<IPAElement> tape) {
		final List<Range> retVal = new ArrayList<Range>();
		final IPATranscript t = new IPATranscript(tape);
		final PhonexMatcher matcher = pattern.matcher(t);
		
		int currentIdx = 0;
		while(matcher.find(currentIdx)) {
			final int start = matcher.start();
			final int end = matcher.end();
			
			retVal.add(new Range(start, end));
			currentIdx = end;
		}
		
		return retVal;
	}

}
