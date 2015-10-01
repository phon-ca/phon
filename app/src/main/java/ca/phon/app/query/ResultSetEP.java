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

import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.FocusManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

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
		final AtomicReference<Project> projectRef = new AtomicReference<>();
		final AtomicReference<Project> tempProjectRef = new AtomicReference<>();
		final AtomicReference<Query> queryRef = new AtomicReference<>();
		final AtomicReference<ResultSet> resultSetRef = new AtomicReference<>();
		final AtomicReference<Boolean> openSessionRef = new AtomicReference<>(Boolean.TRUE);
		
		// check args
		if(initInfo.get("project") == null) {
			throw new IllegalArgumentException("project must be given");
		} else {
			projectRef.set((Project)initInfo.get("project"));
		}
		
		if(initInfo.get("tempProject") != null) {
			tempProjectRef.set((Project)initInfo.get("tempProject"));
		}
		
		// query is optional
		queryRef.set((Query)initInfo.get("query"));
		
		if(initInfo.get("resultset") == null) {
			throw new IllegalArgumentException("resultset must be given");
		} else {
			resultSetRef.set((ResultSet)initInfo.get("resultset"));
		}
		
		if(initInfo.get("opensession") != null) {
			openSessionRef.set(Boolean.parseBoolean(initInfo.get("opensession").toString()));
		}
		
		// look for an existing window
		final AtomicReference<ResultSetEditor> windowRef = new AtomicReference<>();
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof ResultSetEditor) {
				final ResultSetEditor rsViewer = (ResultSetEditor)cmf;
				final boolean sameProject = (rsViewer.getProject() == projectRef.get());
				final boolean sameQuery = 
						sameProject && (queryRef.get().getUUID().equals(rsViewer.getQuery().getUUID()));
				final boolean sameResultSet = 
						sameQuery && (resultSetRef.get().getSessionPath().equals(rsViewer.getResultSet().getSessionPath()));
				if(sameResultSet) {
					windowRef.set(rsViewer);
					break;
				}
			}
		}
		
		final Runnable onEDT = () -> {
			if(openSessionRef.get()) {
				openSession(projectRef.get(), resultSetRef.get());
			}
			
			ResultSetEditor window = windowRef.get();
			if(window != null) {
				window.toFront();
				window.requestFocus();
			} else {
				window = new ResultSetEditor(projectRef.get(), queryRef.get(), resultSetRef.get());
				if(tempProjectRef.get() != null)
					window.setTempProject(tempProjectRef.get());
				
				window.setPreferredSize(new Dimension(500, 600));
				window.pack();
	
				// setup location next to editor if attached
				if(window.getEditor() != null) {
					window.positionRelativeTo(SwingConstants.RIGHT, SwingConstants.LEADING, window.getEditor());
				} else {
					window.setLocationByPlatform(true);
				}
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearFocusOwner();
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
				window.setVisible(true);
				window.getTable().grabFocus();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}
	
	private void openSession(Project project, ResultSet rs) {
		final EntryPointArgs epArgs = new EntryPointArgs();
		epArgs.put(EntryPointArgs.PROJECT_OBJECT, project);
		epArgs.put(EntryPointArgs.CORPUS_NAME, rs.getCorpus());
		epArgs.put(EntryPointArgs.SESSION_NAME, rs.getSession());
		epArgs.put("grabFocus", Boolean.FALSE);
		
		try {
			PluginEntryPointRunner.executePlugin("SessionEditor", epArgs);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}
