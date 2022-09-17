/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ipadictionary;

import ca.phon.extensions.*;
import ca.phon.plugin.PluginManager;
import ca.phon.util.*;
import ca.phon.util.resources.ResourceLoader;

import java.util.*;

/**
 * Manages the library of available IPA dictionaries.
 */
public class IPADictionaryLibrary implements IExtendable {
	
	public final static String DEFAULT_IPA_DICTIONARY_PROP = IPADictionaryLibrary.class.getName() + ".defaultDictionary";
	
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
	
	/**
	 * Get the shared library instance.
	 * 
	 * @return the shared static instance
	 */
	public static IPADictionaryLibrary getInstance() {
		return _instance;
	}
	
	private IPADictionaryLibrary() {
		setupLoaders();
	}
	
	private void setupLoaders() {
		// add the default dictionary handler
//		getLoader().addHandler(new DefaultDictionaryProvider());
		final ServiceLoader<DictionaryProvider> providers = 
				ServiceLoader.load(DictionaryProvider.class, PluginManager.getInstance());
		for(DictionaryProvider provider:providers) {
			resLoader.addHandler(provider);
		}
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
	public Set<Language> availableLanguages() {
		final Set<Language> retVal = new TreeSet<Language>();
		
		final Iterator<IPADictionary> iterator = availableDictionaries();
		while(iterator.hasNext()) {
			final IPADictionary dict = iterator.next();
			final Language lang = dict.getLanguage();
			retVal.add(lang);
		}
		
		return retVal;
	}
	
	/**
	 * Get default IPA dictionary language
	 * 
	 * @return default language
	 */
	public Language getDefaultLanguage() {
		Language retVal = null;
		
		final String langPref = PrefHelper.get(DEFAULT_IPA_DICTIONARY_PROP, "eng");
		if(langPref != null) {
			retVal = Language.parseLanguage(langPref);
		}
		
		return retVal;
	}
	
	/**
	 * Get default IPA dictionary, if no preference is set
	 * this will return the first dictionary found.
	 * 
	 * @return default IPA dictionary
	 */
	public IPADictionary defaultDictionary() {
		final Language defLang = getDefaultLanguage();
		IPADictionary retVal = null;
		if(defLang != null) {
			final List<IPADictionary> dicts = dictionariesForLanguage(defLang);
			if(dicts.size() > 0) {
				retVal = dicts.get(0);
			}
		}
		if(retVal == null && availableDictionaries().hasNext()) {
			retVal = availableDictionaries().next();
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
	public List<IPADictionary> dictionariesForLanguage(String lang) {
		final Language l = Language.parseLanguage(lang);
		return dictionariesForLanguage(l);
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
