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
