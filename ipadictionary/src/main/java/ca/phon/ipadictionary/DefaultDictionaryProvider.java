package ca.phon.ipadictionary;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import ca.phon.ipadictionary.impl.CompoundDictionary;
import ca.phon.ipadictionary.impl.DatabaseDictionary;
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.ipadictionary.impl.ImmutablePlainTextDictionary;
import ca.phon.plugin.PluginManager;
import ca.phon.util.Language;
import ca.phon.util.resources.ClassLoaderHandler;

public class DefaultDictionaryProvider extends ClassLoaderHandler<IPADictionary> 
	implements DictionaryProvider {

	private final static String DICT_LIST = "META-INF/dict/dicts.list";
	
	private Set<Language> dbDicts = null;
	
	public DefaultDictionaryProvider() {
		super(PluginManager.getInstance());
		loadResourceFile(DICT_LIST);
	}
	
	@Override
	public IPADictionary loadFromURL(URL url) throws IOException {
		final IPADictionary immutableDict = 
				new IPADictionary(new ImmutablePlainTextDictionary(url));
		final IPADictionary databaseDict =
				new IPADictionary(new DatabaseDictionary(immutableDict.getLanguage()));
		
		dbDicts.remove(immutableDict.getLanguage());
		
		final CompoundDictionary compoundDict =
				new CompoundDictionary(new IPADictionary[]{ databaseDict, immutableDict });
		return new IPADictionary(compoundDict);
	}

	@Override
	public Iterator<IPADictionary> iterator() {
		dbDicts = IPADatabaseManager.getInstance().getAvailableLanguages();
		return new CustomIterator(super.iterator());
	}
	
	private class CustomIterator implements Iterator<IPADictionary> {
		
		private Iterator<IPADictionary> itr;
		
		private Iterator<Language> langItr = null;
		
		public CustomIterator(Iterator<IPADictionary> itr) {
			this.itr = itr;
		}

		@Override
		public boolean hasNext() {
			return (itr.hasNext() ? true : (langItr != null ? langItr.hasNext() : false));
		}

		@Override
		public IPADictionary next() {
			IPADictionary retVal = null;
			
			if(langItr != null) {
				Language lang = langItr.next();
				retVal = new IPADictionary(new DatabaseDictionary(lang));
				if(!langItr.hasNext())
					langItr = null;
			} else {
				retVal = itr.next();
				if(!itr.hasNext()) {
					langItr = dbDicts.iterator();
				}
			}
			
			return retVal;
		}

		@Override
		public void remove() {
		}
		
	}
	
}
