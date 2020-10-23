/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.query.script;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.apache.logging.log4j.*;

import ca.phon.extensions.*;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.util.*;
import ca.phon.util.resources.*;

/**
 * Script library for phon query scripts.
 *
 *
 */
public final class QueryScriptLibrary implements IExtendable {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(QueryScriptLibrary.class.getName());

	/**
	 * User script folder
	 */
	public final static String USER_SCRIPT_FOLDER =
			PrefHelper.getUserDataFolder() + File.separator + "query";

	private final ExtensionSupport extSupport = new ExtensionSupport(QueryScriptLibrary.class, this);

	private final ResourceLoader<QueryScript> systemScriptLoader = new ResourceLoader<QueryScript>();

	private final ResourceLoader<QueryScript> userScriptLoader = new ResourceLoader<QueryScript>();

	private final ResourceLoader<QueryScript> pluginScriptLoader = new ResourceLoader<QueryScript>();

	public static String projectScriptFolder(Project project) {
		return project.getResourceLocation() + File.separator + "query";
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

		final UserFolderScriptHandler userFolderScriptHandler = new UserFolderScriptHandler(new File(USER_SCRIPT_FOLDER), ScriptLibrary.USER);
		userScriptLoader.addHandler(userFolderScriptHandler);

		// plug-ins
		final List<IPluginExtensionPoint<QueryScriptHandler>> queryScriptHandlers =
				PluginManager.getInstance().getExtensionPoints(QueryScriptHandler.class);
		for(IPluginExtensionPoint<QueryScriptHandler> queryScriptHandler:queryScriptHandlers) {
			pluginScriptLoader.addHandler(queryScriptHandler.getFactory().createObject());
		}
	}


	/**
	 * Find scripts with given name in stock and user script locations.
	 *
	 * @param name name of script (without extension)
	 * @return
	 */
	public List<QueryScript> findScriptsWithName(final String name) {
		return findScriptsWithName(name, null);
	}

	/**
	 * Find scripts with given name in all script locations.  Project is optioal.
	 *
	 * @param name name of script (without extension)
	 * @param project may be <code>null</code>
	 * @return
	 */
	public List<QueryScript> findScriptsWithName(final String name, Project project) {
		final List<QueryScript> retVal = new ArrayList<>();

		Consumer<QueryScript> search = (QueryScript qs) -> {
			final QueryName qn = qs.getExtension(QueryName.class);
			if(qn != null && qn.getName().equals(name)) {
				retVal.add(qs);
			}
		};

		stockScriptFiles().forEach(search);
		userScriptFiles().forEach(search);
		if(project != null) {
			projectScriptFiles(project).forEach(search);
		}

		return retVal;
	}

	public ResourceLoader<QueryScript> stockScriptFiles() {
		return systemScriptLoader;
	}

	public ResourceLoader<QueryScript> userScriptFiles() {
		return userScriptLoader;
	}

	public ResourceLoader<QueryScript> projectScriptFiles(Project project) {
		final ResourceLoader<QueryScript> retVal = new ResourceLoader<QueryScript>();

		final UserFolderScriptHandler userFolderScriptHandler = new UserFolderScriptHandler(new File(projectScriptFolder(project)), ScriptLibrary.PROJECT);
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

			final QueryName qn = qs.getExtension(QueryName.class);

			final QueryManager qm = QueryManager.getSharedInstance();
			final QueryFactory qf = qm.createQueryFactory();
			final Query q = qm.createQueryFactory().createQuery();
			q.setName(qn.getName());
			
			final Script s = qf.createScript();
			if(qn.getScriptLibrary() == ScriptLibrary.STOCK) {
				s.setUrl(new ScriptURL(qn.getName(), qn.getScriptLibrary()));
			} else {
				s.setSource(qs.getScript());
			}

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
				LOGGER.error( e.getLocalizedMessage(), e);
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
