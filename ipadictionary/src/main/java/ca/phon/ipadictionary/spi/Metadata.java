package ca.phon.ipadictionary.spi;

import java.util.Iterator;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Metadata consists of a map of string to string
 * values.
 */
@Extension(IPADictionary.class)
public interface Metadata {
	
	/**
	 * Get value for a given metadata key.
	 * 
	 * @param key the metadata key.  Common keys are
	 *  'provider' and 'website'
	 * @return the value for the specified key or <code>null</code>
	 *  if no data is available. See {@link #metadataKeyIterator()}
	 */
	public String getMetadataValue(String key);
	
	/**
	 * Get the iteator for metadata keys.
	 * 
	 * @return an iterator for the metadata keys available
	 */
	public Iterator<String> metadataKeyIterator();

}
