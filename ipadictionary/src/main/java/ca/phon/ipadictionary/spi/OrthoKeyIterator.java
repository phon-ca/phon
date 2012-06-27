package ca.phon.ipadictionary.spi;

import java.util.Iterator;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Capability for iterating the orthographic keys found
 * in the dictionary.
 * 
 */
@Extension(IPADictionary.class)
public interface OrthoKeyIterator {
	
	/**
	 * Return an iterator for the keys found
	 * in this dictionary.  Order of keys returned
	 * by the iterator is determined by dictionary
	 * implementation and is not guaranteed.
	 * 
	 * @return the key iterator
	 */
	public Iterator<String> iterator();

}
