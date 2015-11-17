package ca.phon.session.check;

/**
 * Listener for session validation events.
 * 
 * @author Greg
 */
public interface ValidationListener {

	public void validationInfo(ValidationEvent ve);
	
}
