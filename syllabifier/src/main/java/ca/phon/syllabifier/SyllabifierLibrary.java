package ca.phon.syllabifier;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.util.resources.ResourceLoader;

/**
 * 
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
	private final ResourceLoader<SyllabifierLibrary> resLoader = 
			new ResourceLoader<SyllabifierLibrary>();
	
	/**
	 * Constructor
	 */
	public SyllabifierLibrary() {
		extSupport.initExtensions();
		
		// load syllabifiers from classpath
		
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
