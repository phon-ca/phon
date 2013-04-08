package ca.phon.xml;

import java.io.IOException;

import javax.xml.stream.XMLEventReader;

/**
 * A class that reads objects from a given XML stream.
 * 
 */
public interface XMLObjectReader {

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
	public <T> T read(XMLEventReader eventReader, Class<T> type)
		throws IOException;
	
}
