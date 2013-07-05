package ca.phon.ipadictionary;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
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
	
	public IPADictionaryLibrary() {
		setupLoader();
	}
	
	private void setupLoader() {
		// add the default dictionary handler
		getLoader().addHandler(new DefaultDictionaryProvider());
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
