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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.*;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.query.script.*;

/**
 * Show the query editor window.
 * 
 */
@PhonPlugin
public class QueryEditorEP implements IPluginEntryPoint {
	
	private static final Logger LOGGER = Logger
			.getLogger(QueryEditorEP.class.getName());

	public final static String EP_NAME = "QueryEditor";
	
	/**
	 * Entry point arguments for loading the script
	 */
	public final static String SCRIPT_OBJECT = QueryEditorEP.class.getName() + "scriptObj";
	public final static String SCRIPT_PATH = QueryEditorEP.class.getName() + "scriptPath";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	public QueryScript getScript(Map<String, Object> args) {
		QueryScript retVal = null;
		
		final Object scriptObj = args.get(SCRIPT_OBJECT);
		if(scriptObj == null) {
			final Object scriptPathObj = args.get(SCRIPT_PATH);
			final String scriptPath = 
					(scriptPathObj != null ? scriptPathObj.toString() : null);
			if(scriptPath != null) {
				final File scriptFile = new File(scriptPath);
				if(scriptFile.exists()) {
					try {
						retVal = new QueryScript(scriptFile.toURI().toURL());
					} catch (MalformedURLException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(),
								e);
					}
				}
			}
		} else {
			if(scriptObj instanceof QueryScript) {
				retVal = QueryScript.class.cast(scriptObj);
			}
		}
		
		return retVal;
	}
	
	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		
		final Project project = epArgs.getProject();
		final QueryScript script = getScript(epArgs);
		
		// check to make sure we have necessary resources
		if(project == null || script == null) return;
		
		QueryName queryName = script.getExtension(QueryName.class);
		if(queryName == null)
			queryName = new QueryName("Untitled");
		
		final QueryEditorWindow queryEditor = new QueryEditorWindow(queryName.getName(), project, script);
		queryEditor.setWindowName(queryName.getName());
		queryEditor.pack();
		queryEditor.setLocationByPlatform(true);
		queryEditor.setVisible(true);
	}

}
