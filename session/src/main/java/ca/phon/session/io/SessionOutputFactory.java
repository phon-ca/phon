package ca.phon.session.io;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory for creating {@link SessionWriter}s
 *
 */
public class SessionOutputFactory {

	/**
	 * Service loader
	 */
	private final ServiceLoader<SessionWriter> writerLoader;
	
	/**
	 * Constructor
	 */
	public SessionOutputFactory() {
		super();
		writerLoader = ServiceLoader.load(SessionWriter.class);
	}
	
	public SessionOutputFactory(ClassLoader cl) {
		super();
		writerLoader = ServiceLoader.load(SessionWriter.class, cl);
	}
	
	/**
	 * Create a new session writer
	 * 
	 * @return a new {@link SessionWriter} or <code>null</code>
	 *  if a compatible writer could not be found
	 */
	public SessionWriter createWriter() {
		SessionWriter retVal = null;
		
		final Iterator<SessionWriter> writerItr = writerLoader.iterator();
		if(writerItr.hasNext()) {
			retVal = writerItr.next();
		}
		
		return retVal;
	}
	
	/**
	 * Create a new session writer for the given version name.
	 * 
	 * @param version
	 * @return a new {@link SessionWriter} or <code>null</code>
	 *  if a writer for the version is not found
	 */
	public SessionWriter createWriter(String version) {
		SessionWriter retVal = null;
		
		final Iterator<SessionWriter> writerItr = writerLoader.iterator();
		while(writerItr.hasNext()) {
			final SessionWriter writer = writerItr.next();
			final SessionIO sessionIO = writer.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.version().equals(version)) {
				retVal = writer;
				break;
			}
		}
		
		return retVal;
	}
}
