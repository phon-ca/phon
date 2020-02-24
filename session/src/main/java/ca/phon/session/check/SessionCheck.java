/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.session.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
	 * 
	 * @return true if session was modified, false otherwise
	 */
	public boolean checkSession(SessionValidator validator, Session session);
	
	public Properties getProperties();
	
	public void loadProperties(Properties props);
	
}
