package ca.phon.syllabifier;

import ca.phon.capability.Capability;
import ca.phon.ipa.phone.Phone;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.syllable.SyllableStress;

/**
 * Adds syllabification information to Phones.
 * 
 * Syllabification information includes:
 *  
 * <ul>
 *   <li># of syllables in phrase</li>
 *   <li>syllable index of phone</li>
 *   <li>syllable stress</li>
 *   <li>constituent type</li>
 * </ul>
 */
@Capability(Phone.class)
public interface SyllabificationInfo {
	
	/**
	 * Return the syllable constituent type.
	 * 
	 * @return the syllable constituent type for
	 *  the phone
	 */
	public SyllableConstituentType getConstituentType();
	
	/**
	 * Stress of the parent syllable.
	 * 
	 * @return the stress type of the parent syllable
	 */
	public SyllableStress getStress();
	
}
