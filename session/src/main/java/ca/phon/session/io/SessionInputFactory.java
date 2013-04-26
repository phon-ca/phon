package ca.phon.session.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Used to create instances of session readers.
 *
 */
public class SessionInputFactory {
	
	/**
	 * Service loader
	 */
	private final ServiceLoader<SessionReader> readerLoader;
	
	/**
	 * Constructor
	 */
	public SessionInputFactory() {
		super();
		readerLoader = ServiceLoader.load(SessionReader.class);
	}
	
	public SessionInputFactory(ClassLoader cl) {
		super();
		readerLoader = ServiceLoader.load(SessionReader.class, cl);
	}
	
	/**
	 * Create a new session reader for the given URI
	 * 
	 * @param uri
	 * @return a new {@link SessionReader} or <code>null</code>
	 *  if a compatible reader could not be found
	 */
	public SessionReader createReader(URI uri) {
		SessionReader retVal = null;
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			final SessionReader reader = readerItr.next();
			try {
				if(reader.canRead(uri)) {
					retVal = reader;
					break;
				}
			} catch (IOException e) {
				// ignore
			}
		}
		
		return retVal;
	}

}
