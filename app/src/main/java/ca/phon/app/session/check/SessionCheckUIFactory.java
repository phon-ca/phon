package ca.phon.app.session.check;

import ca.phon.plugin.*;
import ca.phon.session.check.*;

/**
 * Create configuration forms for {@link SessionCheck}s.
 *
 */
public class SessionCheckUIFactory {
	
	public SessionCheckUIFactory() {
		super();
	}
	
	/**
	 * Create UI for the given session check.  Will return <code>null</code>
	 * if no UI is registered for the given check.
	 * 
	 * @param check
	 * @return UI panel for check or <code>null</code>
	 */
	public SessionCheckUI createUI(SessionCheck check) {
		Class<? extends SessionCheck> checkType = check.getClass();
		
		for(IPluginExtensionPoint<SessionCheckUI> extPt:PluginManager.getInstance().getExtensionPoints(SessionCheckUI.class)) {
			SessionCheckTarget targetType = extPt.getClass().getAnnotation(SessionCheckTarget.class);
			if(targetType != null && targetType.value() == checkType) {
				return extPt.getFactory().createObject(check);
			}
		}
		return null;
	}

}
