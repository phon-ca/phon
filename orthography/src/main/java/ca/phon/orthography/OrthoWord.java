package ca.phon.orthography;


/**
 * A word in an orthographic phrase.
 * Words may have prefix and/or suffix
 * codes.
 */
public class OrthoWord implements OrthoElement {
	
	private final WordPrefix prefix;
	
	private final WordSuffix suffix;
	
	private final String data;
	
	public OrthoWord(String data) {
		this(data, null, null);
	}

	public OrthoWord(String data, WordPrefix prefix) {
		this(data, prefix, null);
	}
	
	public OrthoWord(String data, WordSuffix suffix) {
		this(data, null, suffix);
	}
	
	public OrthoWord(String data, WordPrefix prefix, WordSuffix suffix) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.data = data;
	}
	
	/**
	 * Get prefix for word.
	 * 
	 * @return the word prefix, or <code>null</code> if
	 *  none
	 */
	public WordPrefix getPrefix() {
		return this.prefix;
	}
	
	/**
	 * Get suffix for word.
	 * 
	 * @return the word suffix, or <code>null</code> if
	 *  none
	 */
	public WordSuffix getSuffix() {
		return this.suffix;
	}
	
	/**
	 * Get the root word data (without prefix/suffix)
	 * 
	 * @return the root word 
	 */
	public String getWord() {
		return this.data;
	}
	
	@Override
	public String text() {
		return (
			(this.prefix == null ? "" : this.prefix.getCode()) + 
			this.data + 
			(this.suffix == null ? "" : "@" + this.suffix.getCode())
		);
	}
	
}
