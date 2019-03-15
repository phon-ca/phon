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
package ca.phon.app.menu.query;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.query.QueryAndReportWizardEP;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;
import ca.phon.query.db.ScriptLibrary;
import ca.phon.query.db.xml.XMLScript;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;

/**
 * Open the query script editor with given script.
 */
public class QueryScriptCommand extends PluginAction {
	
	private static final long serialVersionUID = 8363096701935087861L;

	public QueryScriptCommand(Project project, QueryScript script) {
		super(QueryAndReportWizardEP.EP_NAME);

		putArg(EntryPointArgs.PROJECT_OBJECT, project);
		putArg(QueryAndReportWizardEP.SCRIPT_OBJECT, script);
		
		QueryName qn = script.getExtension(QueryName.class);
		if(qn == null) {
			qn = new QueryName("Query");
		}
		putValue(NAME, qn.getName() + "...");
		
		// don't load previous settings if a stock library
		// or if settings have been defined in an xml file
		if(qn.getScriptLibrary() != ScriptLibrary.STOCK
				|| qn.getLocation().getPath().endsWith(".xml")) {
			putArg(QueryAndReportWizardEP.LOAD_PREVIOUS, Boolean.FALSE);
		}
	}
	
}
