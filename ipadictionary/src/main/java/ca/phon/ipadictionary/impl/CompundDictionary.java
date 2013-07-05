package ca.phon.ipadictionary.impl;

import ca.phon.ipadictionary.DictLang;
import ca.phon.ipadictionary.IPADictionary;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.ipadictionary.spi.NameInfo;
import ca.phon.ipadictionary.spi.OrthoKeyIterator;
import ca.phon.ipadictionary.spi.PrefixSearch;
import ca.phon.util.LanguageEntry;

/**
 * Perform lookups on multiple dictionaries at once.
 * 
 */
public class CompundDictionary implements IPADictionarySPI,
	NameInfo, LanguageInfo, PrefixSearch {
	
	/**
	 * Dictionaries
	 * 
	 */
	private List<IPADictionary> dicts;

	@Override
	public String[] lookup(String orthography) throws IPADictionaryExecption {
		Set<String> allTranscripts = new TreeSet<String>();
		
		for(IPADictionary dict:dicts) {
			for(String s:dict.lookup(orthography)) {
				allTranscripts.add(s);
			}
		}
		
		return allTranscripts.toArray(new String[0]);
	}

	@Override
	public String getName() {
		return "Compound";
	}
	
	private LanguageInfo getLanguageInfo() {
		LanguageInfo detectedLang = new DictLang();
		if(dicts.size() > 0) {
			detectedLang = dicts.get(0).getLanguage();
			
			for(int i = 1; i < dicts.size(); i++) {
				if(!dicts.get(i).getLanguage().equals(detectedLang)) {
					detectedLang = new DictLang();
					break;
				}
			}
		}
		
		return detectedLang;
	}

	@Override
	public LanguageEntry getLanguage() {
		return getLanguageInfo().getLanguage();
	}
	
	@Override
	public String[] getUserIds() {
		return getLanguageInfo().getUserIds();
	}

	@Override
	public String[] keysWithPrefix(String prefix) {
		Set<String> keys = new TreeSet<String>();
		
		for(IPADictionary dict:dicts) {
			try {
				for(String key:dict.prefixSearch(prefix)) {
					keys.add(key);
				}
			} catch (IPADictionaryExecption e) {
				e.printStackTrace();
			}
		}
		
		return keys.toArray(new String[0]);
	}
	
}
