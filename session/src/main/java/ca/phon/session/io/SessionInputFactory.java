package ca.phon.session.io;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	 * Get the list of available session readers.
	 * 
	 * @return list of readers
	 */
	public List<SessionIO> availableReaders() {
		final List<SessionIO> retVal = new ArrayList<SessionIO>();
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			// look for the SessionIO annotation
			final SessionReader reader = readerItr.next();
			final SessionIO sessionIO = reader.getClass().getAnnotation(SessionIO.class);
			
			if(sessionIO != null)
				retVal.add(sessionIO);
		}		
		
		return retVal;
	}
	
	/**
	 * Create a new session reader given the SessionIO version.
	 * 
	 * @param id
	 * @param version
	 * @return the new SessionReader or <code>null</code> if not found
	 */
	public SessionReader createReader(String id, String version) {
		SessionReader retVal = null;
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			// look for the SessionIO annotation
			final SessionReader reader = readerItr.next();
			final SessionIO sessionIO = reader.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.version().equals(version) && sessionIO.id().equals(id)) {
				retVal = reader;
				break;
			}
		}
		
		return retVal;
	}
	
}
