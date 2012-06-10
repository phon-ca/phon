package ca.phon.syllabifier;

import java.util.List;

import ca.phon.ipa.phone.Phone;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.LanguageEntry;

/**
 * Provides methods for applying the {@link SyllableConstituentType}
 * annotations on {@link Phone}s.
 * 
 */
public interface Syllabifier {
	
	/**
	 * Syllabifier name.  Preferably unique
	 * for identify syllabifiers in the UI.
	 * 
	 * @return name
	 */
	public String getName();
	
	/**
	 * Syllabifier language.
	 * 
	 * @return language for the syllabifier
	 */
	public LanguageEntry getLanguage();
	
	/**
	 * Apply consituent type annotations
	 * on given phones.
	 * 
	 * @param phones
	 */
	public void syllabify(List<Phone> phones);
	
}
