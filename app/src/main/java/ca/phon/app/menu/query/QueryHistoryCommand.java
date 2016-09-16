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
