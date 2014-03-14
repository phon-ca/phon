package ca.phon.app.menu.query;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.query.QueryEditorEP;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;

/**
 * Open the query script editor with given script.
 */
public class QueryScriptCommand extends PluginAction {
	
	private static final long serialVersionUID = 8363096701935087861L;

	public QueryScriptCommand(Project project, QueryScript script) {
		super(QueryEditorEP.EP_NAME);

		putArg(EntryPointArgs.PROJECT_OBJECT, project);
		putArg(QueryEditorEP.SCRIPT_OBJECT, script);
		
		QueryName qn = script.getExtension(QueryName.class);
		if(qn == null) {
			qn = new QueryName("Query");
		}
		putValue(NAME, qn.getName() + "...");
	}
	
}
