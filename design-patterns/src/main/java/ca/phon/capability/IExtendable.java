package ca.phon.capability;

/**
 * Adds the ability to add/remove capabilites to an
 * object that implements the ICapable interface.
 * 
 * 
 */
public interface IExtendable extends ICapable {

	/**
	 * Add a new capability.
	 * 
	 * @param cap the capability to add
	 * @return the added capability implementation
	 */
	public <T> T putCapability(Class<T> cap, T impl);
	
	/**
	 * Remove a capability.
	 * 
	 * @param cap the capability to remove
	 */
	public void removeCapability(Class<?> cap);
	
}
