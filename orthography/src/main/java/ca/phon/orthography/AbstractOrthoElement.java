package ca.phon.orthography;

import java.util.Set;

import ca.phon.extensions.ExtensionSupport;

/**
 * Abstract implementation of {@link OrthoElement} implementing
 * extension support.
 *
 */
public abstract class AbstractOrthoElement implements OrthoElement {

	private final ExtensionSupport extSupport = new ExtensionSupport(getClass(), this);
	
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
