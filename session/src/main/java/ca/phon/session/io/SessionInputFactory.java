package ca.phon.session.io;

import java.io.IOException;
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
	
	/**
	 * Create a new session reader given the SessionIO version.
	 * 
	 * @param version
	 * @return the new SessionReader or <code>null</code> if not found
	 */
	public SessionReader createReader(String version) {
		SessionReader retVal = null;
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			// look for the SessionIO annotation
			final SessionReader reader = readerItr.next();
			final SessionIO sessionIO = reader.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.version().equals(version)) {
				retVal = reader;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Determine if this reader can accept input
	 * from the given URI
	 * 
	 * @param uri
	 * 
	 * @return <code>true</code> if the reader can read
	 *  data at the given uri, <code>false</code> otherwise
	 *  
	 * @throws IOException if there was a problem
	 *  with the given uri
	 */
	public boolean canRead(URI uri) throws IOException { return true; }
	
}
