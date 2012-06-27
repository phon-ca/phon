package ca.phon.ipadictionary.exceptions;

/**
 * Exceptions thrown when problems are encountered with the
 * dictionary backing storage (e.g., I/O Errors.)
 * 
 * 
 */
public class BackingStoreException extends IPADictionaryExecption {

	public BackingStoreException() {
		super();
	}

	public BackingStoreException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BackingStoreException(String arg0) {
		super(arg0);
	}

	public BackingStoreException(Throwable arg0) {
		super(arg0);
	}
	
}
