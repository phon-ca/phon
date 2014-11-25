package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.orthography.OrthoComment;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoPunct;
import ca.phon.orthography.OrthoPunctType;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Extract only the data we want from the provided
 * ortho element.
 */
public class OrthoElementExtractor extends VisitorAdapter<OrthoElement> {

	private String value = null;
	
	public OrthoElementExtractor() {
		super();
	}
	
	@Override
	public void fallbackVisit(OrthoElement obj) {
		value = obj.toString();
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		value = word.getWord();
	}

	@Visits
	public void visitWordnet(OrthoWordnet wordNet) {
		value = wordNet.getWord1().getWord() + wordNet.getMarker().getMarker() + wordNet.getWord2().getWord();
	}
	
	@Visits
	public void visitComment(OrthoComment comment) {
		// pauses are part of alignment
		if(comment.getData().matches("\\.{1,3}")) {
			value = comment.getData();
		}
	}
	
	public String getValue() {
		return value;
	}
	
}
