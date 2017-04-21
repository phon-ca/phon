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
package ca.phon.app.menu.query;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.query.QueryEditorEP;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;
import ca.phon.query.script.QueryScript;

/**
 * Open an empty query script editor
 *
 */
public class QueryScriptEditorCommand extends PluginAction {

	private static final long serialVersionUID = 2593126628539990253L;

	public QueryScriptEditorCommand(Project project) {
		super(QueryEditorEP.EP_NAME);
		
		putArg(QueryEditorEP.SCRIPT_OBJECT, new QueryScript(""));
		putArg(EntryPointArgs.PROJECT_OBJECT, project);
		
		putValue(NAME, "Script editor...");
	}
	
}
