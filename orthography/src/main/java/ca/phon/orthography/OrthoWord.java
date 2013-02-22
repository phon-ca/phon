package ca.phon.orthography;


/**
 * A word in an orthographic phrase.
 * Words may have prefix and/or suffix
 * codes.
 */
public interface OrthoWord extends OrthoElement {

	/**
	 * Get prefix for word.
	 * 
	 * @return the word prefix, or <code>null</code> if
	 *  none
	 */
	public WordPrefix getPrefix();
	
	/**
	 * Get suffix for word.
	 * 
	 * @return the word suffix, or <code>null</code> if
	 *  none
	 */
	public WordSuffix getSuffix();
	
	/**
	 * Get the root word data (without prefix/suffix)
	 * 
	 * @return the root word 
	 */
	public String getWord();
	
}
