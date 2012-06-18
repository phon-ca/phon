package ca.phon.xml;

import java.io.IOException;

import javax.xml.stream.XMLEventReader;

import ca.phon.extensions.Extension;
import ca.phon.extensions.IExtendable;

/**
 * 
 */
@Extension(IExtendable.class)
public interface XMLReader {

	/**
	 * Read data from the provided
	 * {@link XMLEventReader}.
	 * 
	 * @param reader
	 * @throws IOException if an error occured
	 *  during reading
	 */
	public void read(XMLEventReader reader)
		throws IOException;
	
}
