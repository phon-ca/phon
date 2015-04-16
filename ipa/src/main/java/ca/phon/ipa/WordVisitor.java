/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipa;

import java.util.ArrayList;
import java.util.List;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Breaks a transcript into individual words.
 */
public class WordVisitor extends VisitorAdapter<IPAElement> {

	/**
	 * list of detected syllables
	 */
	private final List<IPATranscript> words = new ArrayList<IPATranscript>();
	
	/**
	 * current syllable
	 * 
	 */
	private IPATranscriptBuilder currentWordBuilder = new IPATranscriptBuilder();
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		appendWord(obj);
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		breakWord();
	}
	
	private void appendWord(IPAElement e) {
		currentWordBuilder.append(e);
	}
	
	private void breakWord() {
		if(currentWordBuilder.toIPATranscript().length() > 0) {
			words.add(currentWordBuilder.toIPATranscript());
			currentWordBuilder = new IPATranscriptBuilder();
		}
	}
	
	public List<IPATranscript> getWords() {
		breakWord();
		return this.words;
	}
	
}
