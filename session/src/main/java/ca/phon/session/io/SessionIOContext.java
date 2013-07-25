package ca.phon.session.io;

/**
 * Context for creating session Reader/Writers of a particular kind.
 * 
 */
public class SessionIOContext {
	
	/**
	 * Create a new session reader.
	 * 
	 * @return a new session reader for this context
	 */
	public SessionReader createReader();
	
	/**
	 * Create a new session writer.
	 * 
	 * @return a new session writer for this context
	 */
	public SessionWriter createWriter();
	
}
