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
package ca.phon.syllable;

import ca.phon.ipa.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.util.*;

/**
 * A phone visitor that breaks an {@link IPATranscript} into syllables.
 */
public class SyllableVisitor extends VisitorAdapter<IPAElement> {
	
	/**
	 * list of detected syllables
	 */
	private final List<IPATranscript> syllables = new ArrayList<IPATranscript>();
	
	/**
	 * current syllable
	 * 
	 */
	protected IPATranscriptBuilder currentSyllableBuilder = new IPATranscriptBuilder();
	
	/**
	 * last phone
	 */
	private IPAElement lastPhone = null;

	@Override
	public void fallbackVisit(IPAElement obj) {
		// everything but basic phones and
		// compound phones act as syllable boundaries
		breakSyllable();
		lastPhone = obj;
	}
	
	@Visits
	public void visitBasicPhone(Phone phone) {
		appendSyllable(phone);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		appendSyllable(phone);
	}
	
	@Visits
	public void visitStressMarker(StressMarker stressMarker) {
		breakSyllable();
		appendSyllable(stressMarker);
	}
	
	@Visits
	public void visitIntraWordPause(IntraWordPause intraWordPause) {
		breakSyllable();
	}
	
	protected void breakSyllable() {
		final IPATranscript currentSyllable = currentSyllableBuilder.toIPATranscript();
		if(currentSyllable.length() > 0) {
			syllables.add(currentSyllable);
			currentSyllableBuilder = new IPATranscriptBuilder();
		}
	}
	
	/**
	 * Get the syllables detected by this visitor
	 * 
	 * @return detected syllables
	 */
	public List<IPATranscript> getSyllables() {
		breakSyllable();
		return new ArrayList<IPATranscript>(syllables);
	}
	
	private void appendSyllable(IPAElement p) {
		if(lastPhone != null) {
			final SyllableConstituentType prevType = lastPhone.constituentType();
			final SyllableConstituentType currentType = p.constituentType();
			
			switch(prevType) {
			case LEFTAPPENDIX:
				if(currentType != SyllableConstituentType.LEFTAPPENDIX &&
					currentType != SyllableConstituentType.ONSET) {
					breakSyllable();
				}
				break;
				
			case ONSET:
				if(currentType != SyllableConstituentType.ONSET &&
					currentType != SyllableConstituentType.NUCLEUS) {
					breakSyllable();
				}
				break;
				
			case NUCLEUS:
				if(currentType == SyllableConstituentType.NUCLEUS) {
                    if(!p.isDiphthong()) {
                        breakSyllable();
                    }
				} else if(currentType != SyllableConstituentType.CODA) {
					breakSyllable();
				}
				break;
				
			case CODA:
				if(currentType != SyllableConstituentType.CODA &&
					currentType != SyllableConstituentType.RIGHTAPPENDIX) {
					breakSyllable();
				}
				break;
				
			case RIGHTAPPENDIX:
				if(currentType != SyllableConstituentType.RIGHTAPPENDIX) {
					breakSyllable();
				}
				break;
				
			case OEHS:
				if(currentType != SyllableConstituentType.OEHS) {
					breakSyllable();
				}
				break;
				
			case UNKNOWN:
				breakSyllable();
				break;
				
			default:
				break;
			}
		}
		currentSyllableBuilder.append(p);
		
		lastPhone = p;
	}

	@Visits
	public void visitPause(Pause pause) {
		// pauses are syllable boundaries
		breakSyllable();
		currentSyllableBuilder.append(pause);
		breakSyllable();
		lastPhone = null;
	}

	/**
	 * Rest syllable list
	 */
	public void reset() {
		this.syllables.clear();
		this.currentSyllableBuilder = new IPATranscriptBuilder();
		this.lastPhone = null;
	}
	
}

