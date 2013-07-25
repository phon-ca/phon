package ca.phon.session.io;

import ca.phon.extensions.Extension;
import ca.phon.session.Session;

/**
 * Extension added to {@link Session} objects to keep track of the
 * original source format.
 * 
 */
@Extension(Session.class)
public class OriginalFormat {

	private SessionIO sessionIO;

	public OriginalFormat() {
		super();
	}
	
	public OriginalFormat(SessionIO io) {
		super();
		setSessionIO(io);
	}

	public SessionIO getSessionIO() {
		return sessionIO;
	}

	public void setSessionIO(SessionIO sessionIO) {
		this.sessionIO = sessionIO;
	}
	
}
