/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

package ca.phon.query.db;

import java.io.*;
import java.util.*;

import ca.phon.plugin.*;


/**
 * A factory class that returns {@link QueryFactory} and
 * {@link ResultSetManager} instances.
 */
public abstract class QueryManager {
	private static QueryManager _sharedInstance = null;
	/**
	 * Return the shared {@link QueryManager) instance
	 * 
	 * @return the shared instance
	 */
	public static QueryManager getSharedInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = getInstance();
		}
		return _sharedInstance;
	}
	
	/**
	 * Gets an instance of a {@link QueryManager}.
	 *  
	 * @return a newly created {@link QueryManager}, or <code>null</code>
	 *         if one could not be found
	 */
	public static QueryManager getInstance() {
		// Get plugins that can create QueryManager instances
		List<IPluginExtensionPoint<QueryManager>> managers = PluginManager.getInstance().getExtensionPoints(QueryManager.class);
		
		// For now, assume there is at least one instance available. When
		// managers beside XMLResultManager exist, we'll need to change this.
		QueryManager manager = null;
		if(managers.size() > 0)
			manager = managers.get(0).getFactory().createObject();
		return manager;
	}
	
	/**
	 * Create an instance of QueryFactory
	 * 
	 * @return a QueryFactory instance
	 */
	public abstract QueryFactory createQueryFactory();
	
	/**
	 * Save a query description to the given path.
	 * 
	 * @param query
	 * @param path
	 * @throws IOException
	 */
	public abstract void saveQuery(Query query, String path)
		throws IOException;
		
	/**
	 * Load query from given path.
	 * 
	 * @param path
	 * @return the loaded Query
	 * @throws IOException
	 */
	public abstract Query loadQuery(String path)
		throws IOException;
	
	/**
	 * Load query from given stream
	 * 
	 * @param stream
	 * @return the loaded Query
	 * @throws IOException
	 */
	public abstract Query loadQuery(InputStream stream)
		throws IOException;
	
	/**
	 * Create an instance of ResultSetManager
	 * 
	 * @return a ResultSetManager instance
	 */
	public abstract ResultSetManager createResultSetManager();
}
