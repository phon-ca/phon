package ca.phon.app.query;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;

import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;

public class QueryAndReportWizardEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "QueryAndReportWizard";
	
	public final static String SCRIPT_OBJECT = QueryAndReportWizard.class.getName() + "scriptObj";
	public final static String SCRIPT_PATH = QueryAndReportWizard.class.getName() + "scriptPath";

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
						LogUtil.log(Level.SEVERE, e.getLocalizedMessage(),
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
		
		final QueryAndReportWizard wizard = new QueryAndReportWizard(project, script);
		wizard.pack();
		wizard.setSize(1024, 768);
		wizard.centerWindow();
		wizard.setVisible(true);
	}

}
