package ca.phon.syllable;

import ca.phon.ipa.IntraWordPause;
import ca.phon.visitor.annotation.Visits;

public class SyllableAndPausesVisitor extends SyllableVisitor {
	
	@Visits
	public void visitPause(IntraWordPause pause) {
		breakSyllable();
		currentSyllableBuilder.append(pause);
		breakSyllable();
	}

}
