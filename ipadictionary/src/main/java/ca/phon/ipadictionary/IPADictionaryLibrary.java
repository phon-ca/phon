package ca.phon.ipadictionary;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.util.Language;
import ca.phon.util.LanguageEntry;
import ca.phon.util.resources.ResourceLoader;

/**
 * Manages the library of available IPA dictionaries.
 */
public class IPADictionaryLibrary implements IExtendable {
	
	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(IPADictionaryLibrary.class, this);

	/**
	 * Resource loader
	 */
	private final ResourceLoader<IPADictionary> resLoader = 
			new ResourceLoader<IPADictionary>();
	
	private final static IPADictionaryLibrary _instance = new IPADictionaryLibrary();
	
	public static IPADictionaryLibrary getInstance() {
		return _instance;
	}
	
	private IPADictionaryLibrary() {
		setupLoader();
	}
	
	private void setupLoader() {
		// add the default dictionary handler
		getLoader().addHandler(new DefaultDictionaryProvider());
	}
	
	/**
	 * Provides an iterator of all available dictionaries.
	 * Dictionaries are in on particular sort order.
	 * 
	 * @return iterator of availble dictionaries
	 */
	public Iterator<IPADictionary> availableDictionaries() {
		return getLoader().iterator();
	}
	
	/**
	 * Get a list of all distinct LanguageInfos available.
	 * 
	 * @return list of availble  languages
	 */
	public Set<Language> availableLangauges() {
		final Set<Language> retVal = new HashSet<Language>();
		
		final Iterator<IPADictionary> iterator = availableDictionaries();
		while(iterator.hasNext()) {
			final IPADictionary dict = iterator.next();
			final Language lang = dict.getLanguage();
			retVal.add(lang);
		}
		
		return retVal;
	}
	
	/**
	 * Get all dictionaries for the specified primary language
	 * 
	 * @param lang
	 * 
	 * @return list of dictionaries for given lang
	 */
	public List<IPADictionary> dictionariesForLanguage(Language lang) {
		final List<IPADictionary> retVal = new ArrayList<IPADictionary>();
		
		final Iterator<IPADictionary> iterator = availableDictionaries();
		while(iterator.hasNext()) {
			final IPADictionary dict = iterator.next();
			final Language l = dict.getLanguage();
			if(l.equals(lang)) {
				retVal.add(dict);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Get the loader used with the library.
	 * 
	 * @return the resource loader
	 */
	public ResourceLoader<IPADictionary> getLoader() {
		return this.resLoader;
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}
	
	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

}
