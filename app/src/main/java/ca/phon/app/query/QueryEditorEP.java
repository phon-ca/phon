package ca.phon.app.query;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;

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
