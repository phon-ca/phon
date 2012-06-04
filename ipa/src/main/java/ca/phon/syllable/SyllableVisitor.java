package ca.phon.syllable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.phone.BasicPhone;
import ca.phon.ipa.phone.CompoundPhone;
import ca.phon.ipa.phone.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * A phone visitor that breaks a list of phones
 * into syllable.  Requires that the {@link SyllabificationInfo}
 * capability is present for each {@link Phone}.
 *
 */
public class SyllableVisitor extends VisitorAdapter<Phone> {
	
	/**
	 * list of detected syllables
	 */
	private final List<IPATranscript> syllables = new ArrayList<IPATranscript>();
	
	/**
	 * current syllable
	 * 
	 */
	private IPATranscript currentSyllable = new IPATranscript();
	
	/**
	 * last phone
	 */
	private Phone lastPhone = null;

	@Override
	public void fallbackVisit(Phone obj) {
		// everything but basic phones and
		// compound phones act as syllable boundaries
		breakSyllable();
		lastPhone = obj;
	}
	
	@Visits
	public void visitBasicPhone(BasicPhone phone) {
		appendSyllable(phone);
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone phone) {
		appendSyllable(phone);
	}
	
	private void breakSyllable() {
		if(currentSyllable.size() > 0) {
			syllables.add(currentSyllable);
			currentSyllable = new IPATranscript();
		}
	}
	
	/**
	 * Get the syllables detected by this visitor
	 * 
	 * @return detected syllables
	 */
	public List<IPATranscript> getSyllables() {
		if(currentSyllable.size() > 0) {
			syllables.add(currentSyllable);
		}
		return Collections.unmodifiableList(syllables);
	}
	
	private void appendSyllable(Phone p) {
		if(lastPhone != null) {
			SyllableConstituentType prevType = lastPhone.getScType();
			SyllableConstituentType currentType = p.getScType();
			
			switch(prevType) {
			case LEFTAPPENDIX:
				if(currentType != SyllableConstituentType.LEFTAPPENDIX ||
					currentType != SyllableConstituentType.ONSET) {
					breakSyllable();
				}
				break;
				
			case ONSET:
				if(currentType != SyllableConstituentType.ONSET ||
					currentType != SyllableConstituentType.NUCLEUS) {
					breakSyllable();
				}
				break;
				
			case NUCLEUS:
				if(currentType == SyllableConstituentType.NUCLEUS) {
					// TODO check for diphthongs
				} else if(currentType != SyllableConstituentType.CODA) {
					breakSyllable();
				}
				break;
				
			case CODA:
				if(currentType != SyllableConstituentType.CODA ||
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
		currentSyllable.add(p);
		
		lastPhone = p;
	}

}
