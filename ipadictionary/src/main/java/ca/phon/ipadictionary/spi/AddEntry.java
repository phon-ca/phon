package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;

/**
 * IPADictionary capability for adding a new
 * orthography->ipa entry.
 *
 */
@Extension(IPADictionary.class)
public interface AddEntry {
	
	/**
	 * Add a new entry to the ipa dictionary
	 * 
	 * @param orthography
	 * @param ipa
	 * @throws IPADictionaryExecption if the entry was
	 *  not added to the dictionary.  E.g., the key->value
	 *  pair already exists or the dictionary was not able
	 *  to add the entry to it's storage.
	 */
	public void addEntry(String orthography, String ipa)
		throws IPADictionaryExecption;
	
}
