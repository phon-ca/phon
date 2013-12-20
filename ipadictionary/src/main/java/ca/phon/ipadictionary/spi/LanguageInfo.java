package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.util.Language;

/**
 * Capability for returning langauge information
 * on a dictionary.
 *
 */
@Extension(IPADictionary.class)
public interface LanguageInfo {

	/**
	 * Returns the language handled by this dictionary.
	 * 
	 * @return the {@link Language} for this
	 *  dictionary
	 */
	public Language getLanguage();
	
}
