package ca.phon.session.io;

import java.io.IOException;
import java.io.OutputStream;

import ca.phon.session.Session;

/**
 * Interface for writing {@link Session} objects
 * to a given {@link OutputStream}
 *
 */
public interface SessionWriter {

	/**
	 * Write the given {@link Session} to the provided
	 * {@link OutputStream}
	 * 
	 * @param session
	 * @param out
	 * 
	 * @throws IOException
	 */
	public void writeSession(Session session, OutputStream out)
		throws IOException;
	
}
