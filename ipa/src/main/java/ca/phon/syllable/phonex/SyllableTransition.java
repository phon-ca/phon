/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.syllable.phonex;

import java.util.*;

import ca.phon.fsa.*;
import ca.phon.ipa.*;
import ca.phon.phonex.*;
import ca.phon.syllable.*;
import ca.phon.util.*;

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
		String retVal = "\u03c3";
		if(syllableRange != null) {
			retVal += "/";
			if(syllableRange.getObj1() != null &&
					syllableRange.getObj1() == syllableRange.getObj2()) {
				retVal += syllableRange.getObj1().getIdChar();
			} else {
				if(syllableRange.getObj1() != null) {
					retVal += syllableRange.getObj1().getIdChar();
				}
				retVal += "..";
				if(syllableRange.getObj2() != null) {
					retVal += syllableRange.getObj2().getIdChar();
				}
			}
			retVal += "/";
		}
		for(PhoneMatcher pm:super.getSecondaryMatchers()) {
			retVal += ":" + pm.toString();
		}
		if(getType() != TransitionType.NORMAL) {
			retVal += " (" + getType() + ")";
		}
		if(getOffsetType() != OffsetType.NORMAL) {
			retVal += " (" + getOffsetType() + ")";
		}
		return retVal;
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
		PhonexTransition.setupTransition(retVal, this.getFirstState(), this.getToState(), "", this.getType(), this.getOffsetType(),
				this.getInitGroups(), getMatcherGroups());
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
