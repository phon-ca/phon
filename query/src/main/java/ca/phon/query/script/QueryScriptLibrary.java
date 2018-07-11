/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.query.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
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
		return project.getResourceLocation() + File.separator + "script";
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

			final QueryName qn = qs.getExtension(QueryName.class);

			final QueryManager qm = QueryManager.getSharedInstance();
			final QueryFactory qf = qm.createQueryFactory();
			final Query q = qm.createQueryFactory().createQuery();

			q.setName((new File(file)).getName());
			final Script s = qf.createScript();

			// TODO save links to scripts instead of the script source
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
