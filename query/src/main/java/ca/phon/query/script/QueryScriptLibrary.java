package ca.phon.query.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.Script;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.util.PrefHelper;
import ca.phon.util.resources.ResourceLoader;

/**
 * Script library for phon query scripts.
 * 
 *
 */
public class QueryScriptLibrary implements IExtendable {

	private static final Logger LOGGER = Logger
			.getLogger(QueryScriptLibrary.class.getName());
	
	/**
	 * User script folder
	 */
	public final static String USER_SCRIPT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "script";
	
	private final ExtensionSupport extSupport = new ExtensionSupport(QueryScriptLibrary.class, this);
	
	private final ResourceLoader<QueryScript> systemScriptLoader = new ResourceLoader<QueryScript>();
	
	private final ResourceLoader<QueryScript> userScriptLoader = new ResourceLoader<QueryScript>();
	
	private final ResourceLoader<QueryScript> pluginScriptLoader = new ResourceLoader<QueryScript>();
	
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
		
		final UserFolderScriptHandler userFolderScriptHandler = new UserFolderScriptHandler(new File(USER_SCRIPT_FOLDER));
		userScriptLoader.addHandler(userFolderScriptHandler);
		
		// plug-ins
		final List<IPluginExtensionPoint<QueryScriptHandler>> queryScriptHandlers = 
				PluginManager.getInstance().getExtensionPoints(QueryScriptHandler.class);
		for(IPluginExtensionPoint<QueryScriptHandler> queryScriptHandler:queryScriptHandlers) {
			pluginScriptLoader.addHandler(queryScriptHandler.getFactory().createObject());
		}
	}
	
	public ResourceLoader<QueryScript> stockScriptFiles() {
		return systemScriptLoader;
	}
	
	public ResourceLoader<QueryScript> userScriptFiles() {
		return userScriptLoader;
	}
	
	public ResourceLoader<QueryScript> projectScriptFiles(Project project) {
		final ResourceLoader<QueryScript> retVal = new ResourceLoader<QueryScript>();
		
		final UserFolderScriptHandler userFolderScriptHandler = new UserFolderScriptHandler(new File(projectScriptFolder(project)));
		retVal.addHandler(userFolderScriptHandler);
		
		return retVal;
	}
	
	public ResourceLoader<QueryScript> pluginScriptFiles(Project project) {
		final ResourceLoader<QueryScript> retVal = new ResourceLoader<QueryScript>();
		
		final List<IPluginExtensionPoint<QueryScriptHandler>> queryScriptHandlers = 
				PluginManager.getInstance().getExtensionPoints(QueryScriptHandler.class);
		for(IPluginExtensionPoint<QueryScriptHandler> queryScriptHandler:queryScriptHandlers) {
			retVal.addHandler(queryScriptHandler.getFactory().createObject());
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
			out.write(script.getScript());
			out.flush();
			out.close();
		} else if(file.endsWith(".xml")) {
			final QueryScript qs = script;
			
			final QueryManager qm = QueryManager.getSharedInstance();
			final QueryFactory qf = qm.createQueryFactory();
			final Query q = qm.createQueryFactory().createQuery();
			
			q.setName((new File(file)).getName());
			final Script s = qf.createScript();
			s.setSource(qs.getScript());

			final Map<String, String> paramMap = new TreeMap<String, String>();
			try {
				for(ScriptParam scriptParam:qs.getContext().getScriptParameters(qs.getContext().getEvaluatedScope())) {
					if(scriptParam.hasChanged()) {
						for(String paramId:scriptParam.getParamIds()) {
							final Object v = scriptParam.getValue(paramId);
							if(v != null) {
								paramMap.put(paramId, v.toString());
							}
						}
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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

	
	
}
