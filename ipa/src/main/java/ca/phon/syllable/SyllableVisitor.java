/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.syllable;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.Phone;
import ca.phon.ipa.StressMarker;
import ca.phon.ipa.StressType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * A phone visitor that breaks a list of phones
 * into syllable.  Requires that the {@link SyllabificationInfo}
 * capability is present for each {@link IPAElement}.
 *
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
	
	protected void breakSyllable() {
		final IPATranscript currentSyllable = currentSyllableBuilder.toIPATranscript();
		if(currentSyllable.length() > 0) {
			// check for stress marker
			final IPAElement firstEle = currentSyllable.elementAt(0);
			SyllableStress stress = SyllableStress.NoStress;
			if(firstEle.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER) {
				final StressType st = StressMarker.class.cast(firstEle).getType();
				stress = (st == StressType.PRIMARY ? SyllableStress.PrimaryStress : SyllableStress.SecondaryStress);
			}
			currentSyllable.putExtension(SyllableStress.class, stress);
			
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
			final SyllableConstituentType prevType = lastPhone.getScType();
			final SyllableConstituentType currentType = p.getScType();
			
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
					final SyllabificationInfo info = p.getExtension(SyllabificationInfo.class);
					if(info != null) {
						if(!info.isDiphthongMember()) {
							breakSyllable();
						}
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

	/**
	 * Rest syllable list
	 */
	public void reset() {
		this.syllables.clear();
		this.currentSyllableBuilder = new IPATranscriptBuilder();
		this.lastPhone = null;
	}
	
}

