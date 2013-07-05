package ca.phon.ipadictionary;

import java.io.IOException;
import java.net.URL;

import ca.phon.ipadictionary.impl.ImmutablePlainTextDictionary;
import ca.phon.util.resources.ClassLoaderHandler;

public class DefaultDictionaryProvider extends ClassLoaderHandler<IPADictionary> {

	private final static String DICT_LIST = "META-INF/dict/dicts.list";
	
	public DefaultDictionaryProvider() {
		super();
		loadResourceFile(DICT_LIST);
	}
	
	@Override
	public IPADictionary loadFromURL(URL url) throws IOException {
		final ImmutablePlainTextDictionary immutableDict = 
				new ImmutablePlainTextDictionary(url);
		return new IPADictionary(immutableDict);
	}

}
