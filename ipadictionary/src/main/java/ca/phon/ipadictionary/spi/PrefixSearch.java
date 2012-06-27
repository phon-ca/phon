package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Performs a search of keys (i.e., orthography) of the dictionary
 * and returns all keys which have the given prefix.
 * 
 */
@Extension(IPADictionary.class)
public interface PrefixSearch {
	
	/**
	 * Search for all instances of the given
	 * prefix in orthographic keys.
	 * 
	 * @param prefix
	 * @return a list of orthographic keys which
	 *  have the specified prefix
	 */
	public String[] keysWithPrefix(String prefix);

}
