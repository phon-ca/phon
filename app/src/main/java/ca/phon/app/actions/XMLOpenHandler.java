package ca.phon.app.actions;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.stream.events.StartElement;

public interface XMLOpenHandler {

	/**
	 * Get supported extensions (without '.')
	 */
	public Set<String> supportedExtensions();
	
	/**
	 * @param startEle
	 * @return <code>true</code> if this reader can handle the given
	 *  root start element.
	 */
	public boolean canRead(StartElement startEle);
	
	/**
	 * Open xml document
	 * 
	 */
	public void openXMLFile(File file) throws IOException;
	
}
