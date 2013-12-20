package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Removes any comments, events, or punctuation
 * from Orthography
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
	
	public List<OrthoElement> getWordList() {
		return this.wordList;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
}
