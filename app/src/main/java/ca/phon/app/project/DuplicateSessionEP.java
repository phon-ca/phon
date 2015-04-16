/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.project;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.Session;

/**
 * Creates a duplicate of a session.
 *
 */
@PhonPlugin(name="default")
public class DuplicateSessionEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER =
			Logger.getLogger(DuplicateSessionEP.class.getName());
	
	private Project project;

	private final static String EP_NAME = "DuplicateSession";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		if(initInfo.get("project") == null
				|| initInfo.get("corpusName") == null
				|| initInfo.get("sessionName") == null)
			throw new IllegalArgumentException("Not enough info given.");
		
		project = (Project)initInfo.get("project");
		String corpus = initInfo.get("corpusName").toString();
		String session = initInfo.get("sessionName").toString();
		
		int idx = 1;
		while(project.getCorpusSessions(corpus).contains(String.format("%s(%d)", session, idx))) {
			++idx;
		}
		final String newSessionName = String.format("%s(%d)", session, idx);
		
		UUID writeLock = null;
		try {
			final Session s = project.openSession(corpus, session);
			s.setName(newSessionName);
			
			writeLock = project.getSessionWriteLock(s);
			project.saveSession(s, writeLock);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			if(writeLock != null) {
				try {
					project.releaseSessionWriteLock(corpus, newSessionName, writeLock);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		
	}

}
