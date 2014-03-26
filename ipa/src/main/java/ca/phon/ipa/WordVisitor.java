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
