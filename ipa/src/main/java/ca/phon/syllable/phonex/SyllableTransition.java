package ca.phon.syllable.phonex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.OffsetType;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.EndOfInputTransition;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexTransition;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Tuple;

/**
 * A transition which will match a full syllable unit
 * or the remainder of a syllable.
 */
public class SyllableTransition extends PhonexTransition {

	private int matchLength = 0;
	
	private PhoneMatcher[] matchers = new PhoneMatcher[0];
	
	private Tuple<SyllableConstituentType, SyllableConstituentType> syllableRange;
	
	public SyllableTransition() {
		this(new PhoneMatcher[0]);
	}
	
	public SyllableTransition(PhoneMatcher[] matchers) {
		this(matchers, null);
	}
	
	public SyllableTransition(PhoneMatcher[] matchers, Tuple<SyllableConstituentType, SyllableConstituentType> range) {
		super(new SyllableMatcher());
		
		this.matchers = matchers;
		this.syllableRange = range;
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
					if(!(checkRange(currentEle))) return false;
					int lastIdx = i;
					for(int j = i; j < syll.length(); j++) {
						IPAElement ele = syll.elementAt(j);
						if(checkRange(ele)) {
							++lastIdx;
							for(PhoneMatcher pm:matchers) {
								if(!pm.matches(ele)) return false;
							}							
						} else {
							break;
						}
					}
					
					matchLength = lastIdx - i;
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean checkRange(IPAElement ele) {
		if(syllableRange == null) return true;
		else {
			SyllableConstituentType sctype = ele.getScType();
		
			boolean afterStart = false;
			switch(syllableRange.getObj1()) {
			case UNKNOWN:
				afterStart = true;
				break;
				
			case SYLLABLESTRESSMARKER:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
				case LEFTAPPENDIX:
				case ONSET:
				case OEHS:
				case NUCLEUS:
				case CODA:
				case RIGHTAPPENDIX:
					afterStart = true;
					break;
					
				default:
					break;
				}
				break;
				
			case LEFTAPPENDIX:
				switch(sctype) {
				case LEFTAPPENDIX:
				case ONSET:
				case OEHS:
				case NUCLEUS:
				case CODA:
				case RIGHTAPPENDIX:
					afterStart = true;
					break;
					
				default:
					break;
				}
				break;
			
			case ONSET:
				switch(sctype) {
				case ONSET:
				case OEHS:
				case NUCLEUS:
				case CODA:
				case RIGHTAPPENDIX:
					afterStart = true;
					break;
					
				default:
					break;
				}
				break;
				
			case NUCLEUS:
				switch(sctype) {
				case NUCLEUS:
				case CODA:
				case RIGHTAPPENDIX:
					afterStart = true;
					break;
					
				default:
					break;
				}
				break;
				
			case CODA:
				switch(sctype) {
				case CODA:
				case RIGHTAPPENDIX:
					afterStart = true;
					break;
					
				default:
					break;
				}
				break;
				
			case RIGHTAPPENDIX:
				switch(sctype) {
				case RIGHTAPPENDIX:
					afterStart = true;
					break;
					
				default:
					break;
				}
				break;
				
			default:
				break;
			}
			
			boolean beforeEnd = false;
			switch(syllableRange.getObj2()) {
			case UNKNOWN:
				beforeEnd = true;
				break;
				
			case RIGHTAPPENDIX:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
				case LEFTAPPENDIX:
				case OEHS:
				case ONSET:
				case NUCLEUS:
				case CODA:
				case RIGHTAPPENDIX:
					beforeEnd = true;
					break;
					
				default:
					break;
				}
				break;
				
			case CODA:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
				case LEFTAPPENDIX:
				case OEHS:
				case ONSET:
				case NUCLEUS:
				case CODA:
					beforeEnd = true;
					break;
					
				default:
					break;
				}
				break;
				
			case NUCLEUS:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
				case LEFTAPPENDIX:
				case OEHS:
				case ONSET:
				case NUCLEUS:
					beforeEnd = true;
					break;
					
				default:
					break;
				}
				break;
				
			case ONSET:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
				case LEFTAPPENDIX:
				case OEHS:
				case ONSET:
					beforeEnd = true;
					break;
					
				default:
					break;
				}
				break;
				
			case LEFTAPPENDIX:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
				case LEFTAPPENDIX:
					beforeEnd = true;
					break;
					
				default:
					break;
				}
				break;
				
			case SYLLABLESTRESSMARKER:
				switch(sctype) {
				case SYLLABLESTRESSMARKER:
					beforeEnd = true;
					break;
					
				default:
					break;
				}
				break;
			
			default:
				break;
			}
			
			return afterStart && beforeEnd;
		}
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
		retVal.matchers = Arrays.copyOf(matchers, matchers.length);
		retVal.syllableRange = (syllableRange != null ? new Tuple<>(syllableRange.getObj1(), syllableRange.getObj2()) : null);
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