package ca.phon.capability;

/**
 * An interface for implementing the 'capability' pattern.
 * 
 * A 'capability' is any class which provides additional
 * (and optional) functionality to a service.
 * 
 */
public interface ICapable {
	
	/**
	 * Return all capabilities supported
	 * 
	 */
	public Class<?>[] getCapabilities();

	/**
	 * Get the requested capability if available.
	 * 
	 * @param class of the requested capability
	 * @return the capability object or <code>null</code> if
	 *  the cability is not available
	 */
	public <T> T getCapability(Class<T> cap);
	
}
