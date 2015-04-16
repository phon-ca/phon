/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.query;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingConstants;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.ResultSet;
import ca.phon.ui.CommonModuleFrame;

/**
 * Display a result set optionally along with the
 * associated session.
 * 
 */
@PhonPlugin(name="default")
public class ResultSetEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(ResultSetEP.class.getName());
	
	private final static String EP_NAME = "ResultSetViewer";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		Project project = null;
		Project tempProject = null;
		Query query = null;
		ResultSet resultSet = null;
		boolean openSession = true;
		
		// check args
		if(initInfo.get("project") == null) {
			throw new IllegalArgumentException("project must be given");
		} else {
			project = (Project)initInfo.get("project");
		}
		
		if(initInfo.get("tempProject") != null) {
			tempProject = (Project)initInfo.get("tempProject");
		}
		
		// query is optional
		query = (Query)initInfo.get("query");
		
		if(initInfo.get("resultset") == null) {
			throw new IllegalArgumentException("resultset must be given");
		} else {
			resultSet = (ResultSet)initInfo.get("resultset");
		}
		
		if(initInfo.get("opensession") != null) {
			openSession = Boolean.parseBoolean(initInfo.get("opensession").toString());
		}
		
		// look for an existing window
		ResultSetEditor window = null;
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof ResultSetEditor) {
				final ResultSetEditor rsViewer = (ResultSetEditor)cmf;
				final boolean sameProject = (rsViewer.getProject() == project);
				final boolean sameQuery = 
						sameProject && (query.getUUID().equals(rsViewer.getQuery().getUUID()));
				final boolean sameResultSet = 
						sameQuery && (resultSet.getSessionPath().equals(rsViewer.getResultSet().getSessionPath()));
				if(sameResultSet) {
					window = rsViewer;
					break;
				}
			}
		}
		
		if(openSession) {
			openSession(project, resultSet);
		}
		
		if(window != null) {
			window.toFront();
			window.requestFocus();
		} else {
			window = new ResultSetEditor(project, query, resultSet);
			if(tempProject != null)
				window.setTempProject(tempProject);
			window.pack();
			// setup location next to editor if attached
			if(window.getEditor() != null) {
				window.positionRelativeTo(SwingConstants.RIGHT, SwingConstants.LEADING, window.getEditor());
			} else {
				window.setLocationByPlatform(true);
			}
			window.setVisible(true);
		}
		
		
	}
	
	private void openSession(Project project, ResultSet rs) {
		final EntryPointArgs epArgs = new EntryPointArgs();
		epArgs.put(EntryPointArgs.PROJECT_OBJECT, project);
		epArgs.put(EntryPointArgs.CORPUS_NAME, rs.getCorpus());
		epArgs.put(EntryPointArgs.SESSION_NAME, rs.getSession());
		
		try {
			PluginEntryPointRunner.executePlugin("SessionEditor", epArgs);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
