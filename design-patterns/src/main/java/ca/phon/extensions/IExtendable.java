package ca.phon.extensions;

import java.util.Set;

/**
 * Adds the ability to add/remove capabilites to an
 * object that implements the ICapable interface.
 * 
 * 
 */
public interface IExtendable {

	/**
	 * Return all extension types supported
	 * 
	 */
	public Set<Class<?>> getExtensions();

	/**
	 * Get the requested extension if available.
	 * 
	 * @param class of the requested capability
	 * @return the capability object or <code>null</code> if
	 *  the cability is not available
	 */
	public <T> T getExtension(Class<T> cap);
	
	/**
	 * Add a new extension.
	 * 
	 * @param cap the extension to add
	 * @return the added extension implementation
	 */
	public <T> T putExtension(Class<T> cap, T impl);
	
	/**
	 * Remove a capability.
	 * 
	 * @param cap the capability to remove
	 */
	public <T> T removeExtension(Class<T> cap);
	
}
