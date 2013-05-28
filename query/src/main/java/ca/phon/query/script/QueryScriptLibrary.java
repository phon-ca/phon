package ca.phon.query.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.phon.application.project.IPhonProject;
import ca.phon.engines.search.db.Query;
import ca.phon.engines.search.db.QueryFactory;
import ca.phon.engines.search.db.QueryManager;
import ca.phon.engines.search.db.Script;
import ca.phon.gui.CommonModuleFrame;
import ca.phon.script.params.ScriptParam;
import ca.phon.system.logger.PhonLogger;
import ca.phon.system.prefs.UserPrefManager;
import ca.phon.util.FileFilter;
import ca.phon.util.NativeDialogs;
import ca.phon.util.resources.ResourceLoader;

/**
 * Script library for phon query scripts.
 * 
 *
 */
public class QueryScriptLibrary {

	/**
	 * Default script location
	 */
	public final static String SYSTEM_SCRIPT_FOLDER = "data/script";
	
	/**
	 * User script folder
	 */
	public final static String USER_SCRIPT_FOLDER = 
			UserPrefManager.getUserPrefDir() + File.separator + "script";
	
	public static String projectScriptFolder(IPhonProject project) {
		return project.getProjectLocation() + File.separator + "__res" + File.separator + "script";
	}
	
	/**
	 * Constructor
	 */
	public QueryScriptLibrary() {
		super();
	}
	
	public List<File> stockScriptFiles() {
		return scanFolderForScripts(new File(SYSTEM_SCRIPT_FOLDER));
	}
	
	public List<File> userScriptFiles() {
		return scanFolderForScripts(new File(USER_SCRIPT_FOLDER));
	}
	
	public List<File> projectScriptFiles(IPhonProject project) {
		final String projectScriptLocation = projectScriptFolder(project);
		return scanFolderForScripts(new File(projectScriptLocation));
	}
	
	private List<File> scanFolderForScripts(File folder) {
		final List<File> retVal = new ArrayList<File>();
		
		if(folder.exists() && folder.isDirectory()) {
			for(File scriptFile:folder.listFiles(scriptFilter)) {
				retVal.add(scriptFile);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Save given script to the specified file
	 * 
	 * @param script
	 * @param file
	 * 
	 * @throws IOException
	 */
	public static void saveScriptToFile(QueryScript script, String file) 
		throws IOException {
		if(file.endsWith(".js")) {
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			out.write(script.getScript(false));
			out.flush();
			out.close();
		} else if(file.endsWith(".xml")) {
			final QueryScript qs = script;
			
			final QueryManager qm = QueryManager.getSharedInstance();
			final QueryFactory qf = qm.createQueryFactory();
			final Query q = qm.createQueryFactory().createQuery();
			
			q.setName((new File(file)).getName());
			final Script s = qf.createScript();
			s.setSource(qs.getScript(false));

			final Map<String, String> paramMap = new TreeMap<String, String>();
			for(ScriptParam scriptParam:qs.getScriptParams()) {
				if(scriptParam.hasChanged()) {
					for(String paramId:scriptParam.getParamIds()) {
						final Object v = scriptParam.getValue(paramId);
						if(v != null) {
							paramMap.put(paramId, v.toString());
						}
					}
				}
			}
			s.setParameters(paramMap);
			q.setScript(s);
			
			qm.saveQuery(q, file);
		}
	}
	
	private final FilenameFilter scriptFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			boolean prefixOk = 
					!(name.startsWith(".") || name.startsWith("~") || name.startsWith("__"));
			boolean suffixOk = 
					(name.endsWith(".js") || name.endsWith(".xml"));
			return prefixOk && suffixOk;
		}
	};
}
