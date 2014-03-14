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
