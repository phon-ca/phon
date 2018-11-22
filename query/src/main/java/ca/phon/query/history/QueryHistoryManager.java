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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.params.history.ParamHistoryManager;
import ca.phon.script.params.history.ParamHistoryType;
import ca.phon.util.PrefHelper;

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
	
	public static void save(QueryHistoryManager manager, QueryScript script) throws IOException {
		final File queryHistoryFile =
				new File(PrefHelper.get(QUERY_HISTORY_FOLDER, DEFAULT_HISTORY_FOLDER), getQueryName(script) + ".xml");
		ParamHistoryManager.saveParamHistory(manager.getParamHistory(), queryHistoryFile);
	}
	
	public static QueryHistoryManager newInstance(String scriptName) throws IOException {
		final File queryHistoryFile = 
				new File(PrefHelper.get(QUERY_HISTORY_FOLDER, DEFAULT_HISTORY_FOLDER), scriptName + ".xml");
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