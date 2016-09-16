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
package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Removes any comments, events, or punctuation
 * from Orthography.  This is used for detecting
 * the number of align-able elements in an Orthography instance.
 *
 */
public class OrthoWordExtractor extends VisitorAdapter<OrthoElement> {

	private final List<OrthoElement> wordList = new ArrayList<OrthoElement>();
	
	@Visits
	public void visitWord(OrthoWord word) {
		wordList.add(word);
	}

	@Visits
	public void visitWordnet(OrthoWordnet wordnet) {
		wordList.add(wordnet);
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		if(comment.getData().matches("\\.{1,3}")) {
			// add pause as an alignment element
			wordList.add(comment);
		}
	}
	
	public List<OrthoElement> getWordList() {
		return this.wordList;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
}
