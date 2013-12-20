package ca.phon.phonex;

import java.util.Arrays;
import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Used to detect syllable boundaries.  Syllable boundaries
 * can be 'hard' (i.e., a '.' or other punctuation) or 'soft'
 * as detected between a (coda, onset) pair.
 * 
 */
public class SyllableBoundaryTransition extends PhonexTransition {

	/**
	 * Length of tape matched
	 */
	private int matchLength = 0;
	
	public SyllableBoundaryTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		boolean retVal = false;
		
		final IPATranscript input = new IPATranscript(Arrays.asList(currentState.getTape()));
		final List<IPATranscript> sylls = input.syllables();
		
		if(currentState.getTapeIndex() == 0 ||
				currentState.getTapeIndex() == currentState.getTape().length) {
			retVal = true;
			matchLength = 0;
		} else {
			final IPAElement p = currentState.getTape()[currentState.getTapeIndex()];
			final PunctuationTest test = new PunctuationTest();
			p.accept(test);
			retVal = test.isPunct;
			if(retVal) {
				matchLength = 1;
			}
		}
		
		return retVal;
	}

	@Override
	public int getMatchLength() {
		return matchLength;
	}
	
	@Override
	public String getImage() {
		return "\\S";
	}
	
	@Override
	public Object clone() {
		SyllableBoundaryTransition retVal = new SyllableBoundaryTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

	/**
	 * Class for testing if a phone is punctuation.
	 * 
	 */
	public class PunctuationTest extends VisitorAdapter<IPAElement> {

		private boolean isPunct = true;
		
		@Override
		public void fallbackVisit(IPAElement obj) {
		}
		
		@Visits
		public void visitPhone(Phone p) {
			isPunct = false;
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			isPunct = false;
		}
		
	}
}
