package ca.phon.ipadictionary.exceptions;

/**
 * Generic IPADictionaryException.  All other dictionary exceptions
 * sub-class this.
 * 
 */
public class IPADictionaryExecption extends Exception {
	
	

	public IPADictionaryExecption() {
		super();
	}

	public IPADictionaryExecption(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public IPADictionaryExecption(String arg0) {
		super(arg0);
	}

	public IPADictionaryExecption(Throwable arg0) {
		super(arg0);
	}

}
