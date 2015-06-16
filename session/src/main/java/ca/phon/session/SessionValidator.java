package ca.phon.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

/**
 * Session validator with plug-in support.  This class maintains the
 * list of available validator plug-ins as well as a set of
 * validation listeners.
 * 
 * @author Greg
 */
public class SessionValidator implements IExtendable {
	
	private final ExtensionSupport extSupport = new ExtensionSupport(SessionValidator.class, this);
	
	private final List<SessionCheck> sessionChecks = new ArrayList<>();
	
	private final List<ValidationListener> listeners = new ArrayList<>();
	
	public SessionValidator() {
	}
	
	public void fireValidationEvent(Session session, String message) {
		fireValidationEvent(new ValidationEvent(session, message));
	}
	
	public void fireValidationEvent(Session session, int record, String message) {
		fireValidationEvent(new ValidationEvent(session, record, message));
	}

	public void fireValidationEvent(Session session, int record, String tierName, int group, String message) {
		fireValidationEvent(new ValidationEvent(session, record, tierName, group, message));
	}
	
	public void fireValidationEvent(final ValidationEvent evt) {
		listeners.forEach( (l) -> { l.validationInfo(evt); } );
	}
	
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

}
