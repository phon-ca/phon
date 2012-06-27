package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;

/**
 * Remove an orthography->ipa pair from the
 * dictionary.  Not all dictionaries support
 * removing entries.
 * 
 */
@Extension(IPADictionary.class)
public interface RemoveEntry {

	/**
	 * Remove the specified entry from the 
	 * dictionary.  Does nothing if the
	 * entry was not found.
	 * 
	 * @param orthography
	 * @param ipa
	 * @throws IPADictionaryExecption if the key->value was not
	 *  removed from the database.  This will usually occur if
	 *  there was a problem with dictionary storage.
	 */
	public void removeEntry(String orthography, String ipa)
		throws IPADictionaryExecption;
	
}
