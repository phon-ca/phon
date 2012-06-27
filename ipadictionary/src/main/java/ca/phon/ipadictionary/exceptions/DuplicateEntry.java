package ca.phon.ipadictionary.exceptions;

import ca.phon.ipadictionary.IPADictionary;

/**
 * Exception thrown during {@link IPADictionary#addEntry(String, String)}
 * when a duplicate entry was specified.
 * 
 * 
 */
public class DuplicateEntry extends IPADictionaryExecption {
	
	/**
	 * Orthography
	 */
	private String orthography;
	
	/**
	 * IPA
	 */
	private String ipa;
	
	

}
