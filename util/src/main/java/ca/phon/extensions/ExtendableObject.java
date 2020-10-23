package ca.phon.extensions;

import java.util.*;

/**
 * Object with extension support enabled.
 *
 */
public class ExtendableObject implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(getClass(), this);
	
	public ExtendableObject() {
		super();
		extSupport.initExtensions();
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

}
