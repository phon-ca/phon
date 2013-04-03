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
	
}
