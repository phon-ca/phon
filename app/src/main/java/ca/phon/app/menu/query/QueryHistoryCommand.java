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
package ca.phon.app.menu.query;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;

/**
 * Open query history window for given project
 */
public class QueryHistoryCommand extends PluginAction {

	private static final long serialVersionUID = 3297971700127087189L;
	
	private final static String EP = "QueryHistory";

	public QueryHistoryCommand(Project project) {
		super(EP);
		
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_OBJECT, project);
		putArgs(args);
		
		putValue(NAME, "Query History...");
	}
	
}
