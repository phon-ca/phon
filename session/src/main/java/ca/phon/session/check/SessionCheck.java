package ca.phon.session.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Session;

public interface SessionCheck {

	/**
	 * Get a list of available session checks.
	 * 
	 * @return all available session checks
	 */
	static public List<SessionCheck> availableChecks() {
		final List<SessionCheck> retVal = new ArrayList<SessionCheck>();
		
		final PluginManager pluginManager = PluginManager.getInstance();
		final List<IPluginExtensionPoint<SessionCheck>> checkExts = pluginManager.getExtensionPoints(SessionCheck.class);
		
		for(IPluginExtensionPoint<SessionCheck> checkPt:checkExts) {
			retVal.add( checkPt.getFactory().createObject() );
		}
		
		return retVal;
	}
	
	/**
	 * Check session and report any issues using the given validator.
	 * 
	 * @param validator
	 * @param session
	 * @param options
	 */
	public void checkSession(SessionValidator validator, Session session, Map<String, Object> options);
	
}
