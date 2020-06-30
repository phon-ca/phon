package ca.phon.syllable.phonex;

import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.OffsetType;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.EndOfInputTransition;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexTransition;

/**
 * A transition which will match a full syllable unit
 * or the remainder of a syllable.
 */
public class SyllableTransition extends PhonexTransition {

	private int matchLength = 0;
	
	private PhoneMatcher[] matchers = new PhoneMatcher[0];
	
	public SyllableTransition() {
		super(new SyllableMatcher());
	}
	
	public SyllableTransition(PhoneMatcher[] matchers) {
		super(new SyllableMatcher());
		
		this.matchers = matchers;
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		matchLength = 0;
		
		int tapeIdx = -1;
		if(getOffsetType() == OffsetType.NORMAL) {
			if(currentState.getTapeIndex() >= currentState.getTape().length) return false;
			tapeIdx = currentState.getTapeIndex();
		} else if(getOffsetType() == OffsetType.LOOK_BEHIND) {
			tapeIdx = currentState.getTapeIndex() - currentState.getLookBehindOffset();
			if(tapeIdx < 0) return false;
		} else if(getOffsetType() == OffsetType.LOOK_AHEAD) {
			tapeIdx = currentState.getTapeIndex() + currentState.getLookAheadOffset();
			if(tapeIdx >= currentState.getTape().length) return false;
		}
		
		final IPATranscript transcript = new IPATranscript(currentState.getTape());
		final List<IPATranscript> sylls = transcript.syllables();
		
		final IPAElement currentEle = currentState.getTape()[tapeIdx];
		
		for(IPATranscript syll:sylls) {
			for(int i = 0; i < syll.length(); i++) {
				if(syll.elementAt(i) == currentEle) {
					for(int j = i; j < syll.length(); j++) {
						IPAElement ele = syll.elementAt(j);
						for(PhoneMatcher pm:matchers) {
							if(!pm.matches(ele)) return false;
						}
					}
					
					matchLength = syll.length() - i;
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public String getImage() {
		return "\u03C3";
	}

	@Override
	public int getMatchLength() {
		return matchLength;
	}
	
	@Override
	public Object clone() {
		SyllableTransition retVal = new SyllableTransition();
		retVal.matchers = matchers;
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

	private static class SyllableMatcher implements PhoneMatcher {

		@Override
		public boolean matches(IPAElement p) {
			return false;
		}

		@Override
		public boolean matchesAnything() {
			return false;
		}
		
	}

}
