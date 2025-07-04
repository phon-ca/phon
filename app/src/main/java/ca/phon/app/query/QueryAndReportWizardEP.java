/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.query;

import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.project.Project;
import ca.phon.query.script.*;
import ca.phon.ui.CommonModuleFrame;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

public class QueryAndReportWizardEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "QueryAndReportWizard";
	
	public final static String SCRIPT_OBJECT = QueryAndReportWizard.class.getName() + ".scriptObj";
	public final static String SCRIPT_PATH = QueryAndReportWizard.class.getName() + ".scriptPath";
	public final static String LOAD_PREVIOUS = QueryAndReportWizard.class.getName() + ".loadPrevious";

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
						LogUtil.severe(e.getLocalizedMessage(),
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
		if(queryName == null) {
			queryName = new QueryName("Untitled");
			script.putExtension(QueryName.class, queryName);
		}
		
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof QueryAndReportWizard) {
				final QueryAndReportWizard openWizard = (QueryAndReportWizard)cmf;
				final QueryName openQueryName = openWizard.getQueryScript().getExtension(QueryName.class);
				
				if(openQueryName.getName().equals(queryName.getName())) {
					openWizard.toFront();
					if(project != openWizard.getExtension(Project.class)) {
						openWizard.newWindow(project);
					}
					return;
				}
			}
		}
		
		QueryAndReportWizardSettings settings = new QueryAndReportWizardSettings();
		if(args.containsKey(LOAD_PREVIOUS)) {
			boolean loadPrevious = Boolean.parseBoolean(args.get(LOAD_PREVIOUS).toString());
			settings.setLoadPreviousExecutionOnStartup(loadPrevious);
		}
		
		final QueryAndReportWizard wizard = new QueryAndReportWizard(project, script, settings);
		wizard.pack();
		wizard.setSize(1024, 768);
		wizard.centerWindow();
		wizard.setVisible(true);
	}

}
