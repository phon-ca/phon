package ca.phon.ipadictionary.exceptions;

/**
 * Exception thrown when a requested capability is not
 * implemented in the dictionary object.
 * 
 */
public class CapabilityNotImplemented extends IPADictionaryExecption {
	
	/**
	 * Requested capability
	 */
	private Class<?> capability;

	public CapabilityNotImplemented(Class<?> cap) {
		super();
		this.capability = cap;
	}

	public CapabilityNotImplemented(Class<?> cap, String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.capability = cap;
	}

	public CapabilityNotImplemented(Class<?> cap, String arg0) {
		super(arg0);
		this.capability = cap;
	}

	public CapabilityNotImplemented(Class<?> cap, Throwable arg0) {
		super(arg0);
		this.capability = cap;
	}
	
	

}
