package ca.phon.ipadictionary;

import java.io.IOException;
import java.net.URL;

import ca.phon.ipadictionary.impl.CompoundDictionary;
import ca.phon.ipadictionary.impl.DatabaseDictionary;
import ca.phon.ipadictionary.impl.ImmutablePlainTextDictionary;
import ca.phon.util.resources.ClassLoaderHandler;

public class DefaultDictionaryProvider extends ClassLoaderHandler<IPADictionary> 
	implements DictionaryProvider {

	private final static String DICT_LIST = "META-INF/dict/dicts.list";
	
	public DefaultDictionaryProvider() {
		super();
		loadResourceFile(DICT_LIST);
	}
	
	@Override
	public IPADictionary loadFromURL(URL url) throws IOException {
		final IPADictionary immutableDict = 
				new IPADictionary(new ImmutablePlainTextDictionary(url));
		final IPADictionary databaseDict =
				new IPADictionary(new DatabaseDictionary(immutableDict.getLanguage()));
		
		final CompoundDictionary compoundDict =
				new CompoundDictionary(new IPADictionary[]{ databaseDict, immutableDict });
		return new IPADictionary(compoundDict);
	}

}
