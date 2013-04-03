package ca.phon.session.io;

import java.io.IOException;

import ca.phon.session.Session;
import java.io.InputStream;
import java.net.URI;

/**
 * Interface for a session reader.  The reader is responsible
 * for reading an {@link InputStream} and returning the resulting
 * session.  Instances of this class should be obtained using
 * {@link SessionInputFactory}.
 *
 */
public interface SessionReader {
	
	/**
	 * Create session from given input stream
	 * 
	 * @param stream
	 * @return session
	 * 
	 * @throws IOException if an error occurs
	 */
	public Session readSession(InputStream in) throws IOException;
	
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
	public boolean canRead(InputStream in) throws IOException;
	
}
