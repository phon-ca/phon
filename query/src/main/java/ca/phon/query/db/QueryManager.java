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

package ca.phon.query.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;


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
