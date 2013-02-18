package ca.phon.syllabifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifierClassLoaderProvider;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;
import ca.phon.util.resources.ResourceLoader;

/**
 * Class to help with loading syllabifiers.
 */
public final class SyllabifierLibrary implements IExtendable {
	
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
	
	/**
	 * Constructor
	 */
	public SyllabifierLibrary() {
		extSupport.initExtensions();
		
		resLoader.addHandler(new OpGraphSyllabifierClassLoaderProvider());
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
	 * Get the first available syllabifier for the given language.
	 * 
	 * @param lang - should be one of the ISO
	 *  3-letter language codes
	 * @return the syllabifier if available,
	 *  <code>null</code> otherwise
	 */
	public Syllabifier getSyllabifierForLanguage(String lang) {
		final LanguageParser parser = LanguageParser.getInstance();
		final LanguageEntry entry = parser.getEntryById(lang);
		return getSyllabifierForLanguage(entry);
	}
	
	public Syllabifier getSyllabifierForLanguage(LanguageEntry entry) {
		Syllabifier retVal = null;
		
		final Iterator<Syllabifier> itr = resLoader.iterator();
		while(itr.hasNext()) {
			final Syllabifier syllabifier = itr.next();
			if(syllabifier.getLanguage().equals(entry)) {
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
		final LanguageParser parser = LanguageParser.getInstance();
		final LanguageEntry entry = parser.getEntryById(lang);
		return getSyllabifiersForLanguage(entry);
	}
	
	public List<Syllabifier> getSyllabifiersForLanguage(LanguageEntry entry) {
		final List<Syllabifier> retVal = new ArrayList<Syllabifier>();
		
		final Iterator<Syllabifier> itr = resLoader.iterator();
		while(itr.hasNext()) {
			final Syllabifier syllabifier = itr.next();
			if(syllabifier.getLanguage().equals(entry)) {
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
		
		final Iterator<Syllabifier> itr = resLoader.iterator();
		while(itr.hasNext()) {
			final Syllabifier syllabifier = itr.next();
			if(syllabifier.getName().equals(name)) {
				retVal = syllabifier;
				break;
			}
		}
		
		return retVal;
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
