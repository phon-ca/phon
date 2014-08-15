package ca.phon.ipadictionary.impl;

import java.util.Set;
import java.util.TreeSet;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.AddEntry;
import ca.phon.ipadictionary.spi.GenerateSuggestions;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.ipadictionary.spi.NameInfo;
import ca.phon.ipadictionary.spi.PrefixSearch;
import ca.phon.ipadictionary.spi.RemoveEntry;
import ca.phon.util.Language;

/**
 * Perform lookups on multiple dictionaries at once.
 * 
 */
public class CompoundDictionary implements IPADictionarySPI,
	NameInfo, LanguageInfo, PrefixSearch {
	
	/**
	 * Dictionaries
	 * 
	 */
	private final IPADictionary[] dicts;

	public CompoundDictionary(IPADictionary[] dicts) {
		this.dicts = dicts;
	}
	
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

	@Override
	public Language getLanguage() {
		Language detectedLang = new Language();
		if(dicts.length > 0) {
			detectedLang = dicts[0].getLanguage();
			
			for(int i = 1; i < dicts.length; i++) {
				if(!dicts[i].getLanguage().equals(detectedLang)) {
					detectedLang.appendUserID(dicts[i].getLanguage().toString());
				}
			}
		}
		
		return detectedLang;
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

	@Override
	public void install(IPADictionary dict) {
		for(IPADictionary d:dicts) {
			final AddEntry addEntry = d.getExtension(AddEntry.class);
			if(addEntry != null) {
				dict.putExtension(AddEntry.class, addEntry);
			}
			final RemoveEntry removeEntry = d.getExtension(RemoveEntry.class);
			if(removeEntry != null) {
				dict.putExtension(RemoveEntry.class, removeEntry);
			}
			final GenerateSuggestions genSuggestions = d.getExtension(GenerateSuggestions.class);
			if(genSuggestions != null) {
				dict.putExtension(GenerateSuggestions.class, genSuggestions);
			}
		}
		
		// override some extensions
		dict.putExtension(NameInfo.class, this);
		dict.putExtension(LanguageInfo.class, this);
		dict.putExtension(PrefixSearch.class, this);
	}
	
}
