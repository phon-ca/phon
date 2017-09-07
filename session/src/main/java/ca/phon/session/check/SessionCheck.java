/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session.check;

import java.util.*;

import ca.phon.plugin.*;
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
