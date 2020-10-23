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
package ca.phon.query.history;

import java.io.*;
import java.util.*;

import ca.phon.query.script.*;
import ca.phon.script.params.history.*;
import ca.phon.util.*;

/**
 * Responsible for loading and saving query history files. Also includes
 * utility methods for some useful query history functions like adding
 * to the history.
 * 
 */
public class QueryHistoryManager extends ParamHistoryManager {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(QueryHistoryManager.class.getName());

	public final static String QUERY_HISTORY_FOLDER = QueryHistoryManager.class.getName() + ".queryHistoryFolder";
	public final static String DEFAULT_HISTORY_FOLDER = PrefHelper.getUserDataFolder() + File.separator + "query_history";
	
	private final static Map<String, QueryHistoryManager> queryHistoryCache = new HashMap<>();
	
	/**
	 * Return the cached query history for the given script.  If no history
	 * is found in the cache the history is loaded (or created) and stored.
	 * 
	 * @param script
	 * @return
	 */
	public static QueryHistoryManager getCachedInstance(QueryScript script) {
		String hash = script.getHashString();
		String queryName = getQueryName(script);
		QueryHistoryManager cachedHistory = queryHistoryCache.get(script.getHashString());
		if(cachedHistory == null) {
			try {
				cachedHistory = QueryHistoryManager.newInstance(script);
			} catch (IOException e) {
				final ObjectFactory factory = new ObjectFactory();
				final ParamHistoryType paramHistory = factory.createParamHistoryType();
				cachedHistory = new QueryHistoryManager(paramHistory);
			}
			
			cachedHistory.getParamHistory().setScript(queryName);
			cachedHistory.getParamHistory().setHash(hash);
			queryHistoryCache.put(hash, cachedHistory);
		}
		return cachedHistory;
	}
	
	public static void clearHistoryCache() {
		queryHistoryCache.clear();
	}
	
	private static String getQueryName(QueryScript script) {
		final QueryName qn = script.getExtension(QueryName.class);
		if(qn != null) {
			return qn.getName();
		} else {
			// use hash of script as name
			return script.getHashString();
		}
	}
	
	public static QueryHistoryManager newInstance(QueryScript script) throws IOException {
		return newInstance(getQueryName(script));
	}
	
	public static File queryHistoryFile(QueryScript script) {
		return queryHistoryFile(getQueryName(script));
	}
	
	public static File queryHistoryFile(String name) {
		final File queryHistoryFile = 
				new File(PrefHelper.get(QUERY_HISTORY_FOLDER, DEFAULT_HISTORY_FOLDER), name + ".xml");
		return queryHistoryFile;
	}
	
	public static void save(QueryHistoryManager manager, QueryScript script) throws IOException {
		final File queryHistoryFile = queryHistoryFile(getQueryName(script));
		ParamHistoryManager.saveParamHistory(manager.getParamHistory(), queryHistoryFile);
	}
	
	public static QueryHistoryManager newInstance(String scriptName) throws IOException {
		final File queryHistoryFile = queryHistoryFile(scriptName);
		return new QueryHistoryManager(queryHistoryFile);
	}
	
	public QueryHistoryManager(File paramHistoryFile) throws IOException {
		super(paramHistoryFile);
	}
	
	public QueryHistoryManager(InputStream inputStream) throws IOException {
		super(inputStream);
	}
	
	public QueryHistoryManager(ParamHistoryType paramHistory) {
		super(paramHistory);
	}
	
}
