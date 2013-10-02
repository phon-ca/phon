package ca.phon.orthography;

import java.util.ArrayList;
import java.util.List;

import ca.phon.visitor.Visitor;
import ca.phon.visitor.annotation.Visits;

/**
 * Removes any comments, events, or punctuation
 * from Orthography
 *
 */
public class OrthoWordExtractor implements Visitor<OrthoElement> {

	private final List<OrthoElement> wordList = new ArrayList<>();
	
	@Override
	public void visit(OrthoElement obj) {
	}
	
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
}
