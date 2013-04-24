package ca.phon.xml;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class that reads objects from a given XML stream.
 * 
 */
public interface XMLObjectReader<T> {

	/**
	 * Read from the given xml input stream.
	 * 
	 * @param eventReader
	 * @param type
	 * 
	 * @return object of given type read from the
	 *  given eventReader
	 *  
	 * @throws IOException if something goes wrong
	 */
	public T read(Document doc, Element ele)
		throws IOException;
	
}
