/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.syllabifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;
import ca.phon.util.resources.ResourceLoader;

/**
 * Class to help with loading syllabifiers.
 */
public final class SyllabifierLibrary implements IExtendable {
	
	public final static String DEFAULT_SYLLABIFIER_LANG_PROP = 
			SyllabifierLibrary.class.getName() + ".defaultSyllabifierLanguage";
	
	private final static String SYLLABIFIER_LIBRARY_LIST = "META-INF/syllabifier.list";
	
	private List<Syllabifier> availableSyllabifiers = null;
	
	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = 
			new ExtensionSupport(SyllabifierLibrary.class, this);
	
	/**
	 * Resource loader
	 */
	private final ResourceLoader<Syllabifier> resLoader = 
			new ResourceLoader<Syllabifier>();
	
	private final static SyllabifierLibrary _instance = new SyllabifierLibrary();
	public static SyllabifierLibrary getInstance() {
		return _instance;
	}
	
	/**
	 * Constructor
	 */
	private SyllabifierLibrary() {
		extSupport.initExtensions();
		
		final ServiceLoader<SyllabifierProvider> loader = ServiceLoader.load(SyllabifierProvider.class);
		final Iterator<SyllabifierProvider> itr = loader.iterator();
		while(itr.hasNext()) {
			final SyllabifierProvider provider = itr.next();
			getLoader().addHandler(provider);
		}
	}
	
	/**
	 * Get syllabifier loader
	 * 
	 * @return {@link ResourceLoader<Syllabifier>}
	 */
	public ResourceLoader<Syllabifier> getLoader() {
		return this.resLoader;
	}
	
	/**
	 * Reload syllabifier definitions.
	 */
	public void reloadDefinitions() {
		if(availableSyllabifiers != null)
			availableSyllabifiers.clear();
		availableSyllabifiers = null;
		availableSyllabifiers();
	}
	
	/**
	 * Return an Iterator for the available syllabifiers.
	 * 
	 * @return iterator for the available syllabifiers
	 * 
	 */
	public Iterator<Syllabifier> availableSyllabifiers() {
		if(availableSyllabifiers == null) {
			availableSyllabifiers = new ArrayList<Syllabifier>();
			final Iterator<Syllabifier> itr = getLoader().iterator();
			while(itr.hasNext()) {
				final Syllabifier syllabifier = itr.next();
				if(syllabifier != null)
					availableSyllabifiers.add(syllabifier);
			}
		}
		return Collections.unmodifiableList(availableSyllabifiers).iterator();
	}
	
	/**
	 * Get a list of available syllabifier names.
	 * 
	 * @return list of names
	 */
	public List<String> availableSyllabifierNames() {
		final List<String> retVal = new ArrayList<String>();
		
		final Iterator<Syllabifier> iterator = availableSyllabifiers();
		while(iterator.hasNext()) {
			final Syllabifier syllabifier = iterator.next();
			retVal.add(syllabifier.getName());
		}
		
		return retVal;
	}
	
	/**
	 * Get a set of available syllabifier Languages
	 * 
	 * @return set of language
	 */
	public Set<Language> availableSyllabifierLanguages() {
		final Set<Language> retVal = new LinkedHashSet<Language>();
		
		final Iterator<Syllabifier> iterator = availableSyllabifiers();
		while(iterator.hasNext()) {
			final Syllabifier syllabifier = iterator.next();
			retVal.add(syllabifier.getLanguage());
		}
		
		return retVal;
	}
	
	/**
	 * Returns a list of syllabifier languages.
	 */
	
	/**
	 * Get the first available syllabifier for the given language.
	 * 
	 * @param lang - should be one of the ISO
	 *  3-letter language codes
	 * @return the syllabifier if available,
	 *  <code>null</code> otherwise
	 */
	public Syllabifier getSyllabifierForLanguage(String lang) {
		final Language language = Language.parseLanguage(lang);
		return getSyllabifierForLanguage(language);
	}
	
	public Syllabifier getSyllabifierForLanguage(Language lang) {
		Syllabifier retVal = null;
		
		final Iterator<Syllabifier> itr = availableSyllabifiers();
		while(itr.hasNext()) {
			final Syllabifier syllabifier = itr.next();
			if(syllabifier.getLanguage().equals(lang)) {
				retVal = syllabifier;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Return all available syllabifiers for the given language.
	 * 
	 * @param ang - should be one of the ISO
	 *  3-letter language codes
	 * @return the list of syllabifiers for the given language.
	 */
	public List<Syllabifier> getSyllabifiersForLanguage(String lang) {
		final Language l = Language.parseLanguage(lang);
		
		return getSyllabifiersForLanguage(l);
	}
	
	public List<Syllabifier> getSyllabifiersForLanguage(Language lang) {
		final List<Syllabifier> retVal = new ArrayList<Syllabifier>();
		
		final Iterator<Syllabifier> itr = availableSyllabifiers();
		while(itr.hasNext()) {
			final Syllabifier syllabifier = itr.next();
			if(syllabifier.getLanguage().equals(lang)) {
				retVal.add(syllabifier);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Get a syllabifier by name.
	 * 
	 * @param name
	 * @return the named syllabifier or <code>null</code>
	 */
	public Syllabifier getSyllabifierByName(String name) {
		Syllabifier retVal = null;
		
		final Iterator<Syllabifier> itr = availableSyllabifiers();
		while(itr.hasNext()) {
			final Syllabifier syllabifier = itr.next();
			if(syllabifier.getName().equals(name)) {
				retVal = syllabifier;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Default syllabifier language
	 * 
	 * @return default syllabifier language
	 */
	public Language defaultSyllabifierLanguage() {
		String lang = PrefHelper.get(DEFAULT_SYLLABIFIER_LANG_PROP, "eng");
		Language retVal = null;
		if(lang != null) {
			retVal = Language.parseLanguage(lang);
		} else {
			if(availableSyllabifierLanguages().size() > 0) {
				retVal = availableSyllabifierLanguages().iterator().next();
			}
		}
		return retVal;
	}
	
	public Syllabifier defaultSyllabifier() {
		final List<Syllabifier> vals = getSyllabifiersForLanguage(defaultSyllabifierLanguage());
		if(vals.size() > 0)
			return vals.get(0);
		return null;
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
