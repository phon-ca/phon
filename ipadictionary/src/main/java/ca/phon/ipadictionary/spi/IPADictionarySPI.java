package ca.phon.ipadictionary.spi;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;

/**
 * Required interface for IPADictionary implementations.
 * IPADictionaries are expected to accept orthographic strings
 * and return one or more associated IPA transcriptions.
 * 
 */
public interface IPADictionarySPI {
	
	/**
	 * Lookup IPA transcriptions for a given
	 * orthographic string.
	 * 
	 * @param orthography
	 * @return a list of IPA transcriptions associated
	 *  with the given orthography
	 * @throws IPADictionary exception if an error occured
	 *  while attempting to lookup the given entry
	 */
	public String[] lookup(String orthography)
		throws IPADictionaryExecption;

	/**
	 * Install this SPI into the given IPADictionary
	 */
	public void install(IPADictionary dict);
}
