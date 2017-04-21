/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import javax.swing.SwingUtilities;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * Display the 'Query History' window {@link QueryHistory}.
 * 
 * Only one {@link QueryHistory} window may be displayed
 * for a project.
 */
@PhonPlugin(name="default")
public class QueryHistoryEP implements IPluginEntryPoint {
	
	private final static String EP_NAME = "QueryHistory";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		// check args
		if(initInfo.get("project") == null)
			throw new IllegalArgumentException("A project must be given.");
		final Project project = (Project)initInfo.get("project");
		
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				// look for an existing query history window
				QueryHistory window = null;
				for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
					if(cmf instanceof QueryHistory) {
						final QueryHistory qh = (QueryHistory)cmf;
						if(qh.getProject() == project) {
							window = (QueryHistory)cmf;
							break;
						}
					}
				}
				
				// bring located window to front, create a new window if existing 
				// query history is not found
				if(window != null) {
					window.toFront();
					window.requestFocus();
				} else {
					window = new QueryHistory(project);
					window.pack();
					window.setLocationByPlatform(true);
					window.setVisible(true);
				}
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
