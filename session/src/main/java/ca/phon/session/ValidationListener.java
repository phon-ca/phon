package ca.phon.session;

/**
 * Listener for session validation events.
 * 
 * @author Greg
 */
public interface ValidationListener {

	public void validationInfo(ValidationEvent ve);
	
}
