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
import java.util.Set;
import java.util.TreeMap;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.Script;
import ca.phon.script.params.ScriptParam;

import ca.phon.util.resources.ResourceLoader;

/**
 * Script library for phon query scripts.
 * 
 *
 */
public class QueryScriptLibrary implements IExtendable {

//	/**
//	 * Default script location
//	 */
//	public final static String SYSTEM_SCRIPT_FOLDER = "data/script";
//	
//	/**
//	 * User script folder
//	 */
//	public final static String USER_SCRIPT_FOLDER = 
//			UserPrefManager.getUserPrefDir() + File.separator + "script";
	
	private final ExtensionSupport extSupport = new ExtensionSupport(QueryScriptLibrary.class, this);
	
	private final ResourceLoader<QueryScript> systemScriptLoader = new ResourceLoader<>();
	
	private final ResourceLoader<QueryScript> userScriptLoader = new ResourceLoader<>();
	
	public static String projectScriptFolder(Project project) {
		return project.getLocation() + File.separator + "__res" + File.separator + "script";
	}
	
	/**
	 * Constructor
	 */
	public QueryScriptLibrary() {
		super();
		extSupport.initExtensions();
		
		initLoaders();
	}
	
	private void initLoaders() {
		final SystemQueryScriptHandler systemScriptHandler = new SystemQueryScriptHandler();
		systemScriptLoader.addHandler(systemScriptHandler);
	}
	
	public ResourceLoader<QueryScript> stockScriptFiles() {
		return systemScriptLoader;
	}
	
	public ResourceLoader<QueryScript> userScriptFiles() {
		return userScriptLoader;
	}
	
	public ResourceLoader<QueryScript> projectScriptFiles(Project project) {
		final String projectScriptLocation = projectScriptFolder(project);
		
		return new ResourceLoader<>();
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
	
	
	
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
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
