package ca.phon.session.io;

import java.io.IOException;
import java.io.InputStream;

import ca.phon.session.Session;

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
	public Session readSession(InputStream stream) throws IOException;
	
}
