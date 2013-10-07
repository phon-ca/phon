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
	private IPATranscript currentWord = new IPATranscript();
	
	@Override
	public void fallbackVisit(IPAElement obj) {
		appendWord(obj);
	}
	
	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		breakWord();
	}
	
	private void appendWord(IPAElement e) {
		currentWord.add(e);
	}
	
	private void breakWord() {
		if(currentWord.size() > 0) {
			words.add(currentWord);
			currentWord = new IPATranscript();
		}
	}
	
	public List<IPATranscript> getWords() {
		breakWord();
		return this.words;
	}
	
}
