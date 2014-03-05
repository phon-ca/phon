package ca.phon.orthography;

import ca.phon.extensions.IExtendable;

public interface OrthoElement extends IExtendable {

	/**
	 * Get string representation of the element.
	 * 
	 * @return the string value of the element
	 */
	public String text();
	
}
