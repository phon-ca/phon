package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Dictionary 'names.' The default name is the uri
 * of the loaded dictionary.
 *
 */
@Extension(IPADictionary.class)
public interface NameInfo {

	/**
	 * Returns a string identifier for this dictionary.
	 * While not required, the name should be unique
	 * to help users identify dictionaries which handle
	 * the same language.
	 * 
	 * @return the dictionary name
	 */
	public String getName();
	
}
