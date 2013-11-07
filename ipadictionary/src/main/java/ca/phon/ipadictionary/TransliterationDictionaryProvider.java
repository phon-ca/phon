package ca.phon.ipadictionary;

import java.io.IOException;
import java.net.URL;

import ca.phon.ipadictionary.impl.TransliterationDictionary;
import ca.phon.util.resources.ClassLoaderHandler;

/**
 * Dictionary which use a tokenizer and lookup table
 * to peform ipa lookups.
 *
 */
public class TransliterationDictionaryProvider extends ClassLoaderHandler<IPADictionary> implements DictionaryProvider {

	private final static String DICT_LIST = "META-INF/dict/transdicts.list";
	
	public TransliterationDictionaryProvider() {
		super();
		loadResourceFile(DICT_LIST);
	}
	
	@Override
	public IPADictionary loadFromURL(URL url) throws IOException {
		final TransliterationDictionary dictImpl = 
				new TransliterationDictionary(url);
		return new IPADictionary(dictImpl);
	}

}
