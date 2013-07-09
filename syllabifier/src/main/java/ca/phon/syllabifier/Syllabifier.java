package ca.phon.syllabifier;

import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;

/**
 * Provides methods for applying the {@link SyllableConstituentType}
 * annotations on {@link IPAElement}s.
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
	public Language getLanguage();
	
	/**
	 * Apply consituent type annotations
	 * on given phones.
	 * 
	 * @param phones
	 */
	public void syllabify(List<IPAElement> phones);
	
}
