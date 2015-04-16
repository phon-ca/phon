/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.util.List;

import ca.phon.project.Project;

/**
 * An interface used for anything that manages the retrieval and storage of
 * queries and result sets from a project. 
 */
public interface ResultSetManager {
	/**
	 * Gets a list of queries for a specified project.
	 * 
	 * @param project  the project
	 * 
	 * @return a list of queries for the given project 
	 */
	public abstract List<Query> getQueries(Project project);
	
	/**
	 * Gets a list of result sets for a specified query and project.
	 * 
	 * @param project  the project
	 * @param query  the query
	 * 
	 * @return the list of result sets for a given query of the given project
	 */
	public abstract List<ResultSet> getResultSetsForQuery(Project project, Query query);
	
	/**
	 * Saves a query in the specified project.
	 * 
	 * @param project  the project
	 * @param query  the query
	 * 
	 * @throws IOException  if the query could not be saved
	 */
	public abstract void saveQuery(Project project, Query query)
			throws IOException;
	
	/**
	 * Loads a given query from the specified project.
	 * 
	 * @param project  the project
	 * @param queryName  the query name
	 * 
	 * @return the query, if successfully loaded
	 * 
	 * @throws IOException  if the query could not be loaded
	 */
	public abstract Query loadQuery(Project project, String queryName)
			throws IOException;
	
	/**
	 * Saves a query's result set in the specified project.
	 * 
	 * @param project  the project
	 * @param query  the query
	 * @param resultSet  the result set
	 * 
	 * @throws IOException  if the result set could not be saved
	 */
	public abstract void saveResultSet(Project project, Query query, ResultSet resultSet)
			throws IOException;
	
	/**
	 * Loads a given result set for a query from the specified project.
	 * 
	 * @param project  the project
	 * @param query  the query
	 * @param sessionName  the session name
	 * 
	 * @return the result set, if successfully loaded
	 * 
	 * @throws IOException  if the result set could not be loaded
	 */
	public abstract ResultSet loadResultSet(Project project, Query query, String sessionName)
			throws IOException;
	
	/**
	 * Delete the given query from the specified project.  This will
	 * also delete all associated result sets.
	 * 
	 * @param project
	 * @param query
	 * 
	 * @throws IOException if the query is not found or could not be removed
	 *  from the storage device.
	 */
	public abstract void deleteQuery(Project project, Query query)
		throws IOException;
	
	/**
	 * Delete the specified result set from the given project and query.
	 * 
	 * @param project
	 * @param query
	 * @param resultset
	 * 
	 * @throws IOException if the resultset/query is not found or could not be
	 *  removed from the storage device.
	 */
	public abstract void deleteResultSet(Project project, Query query, ResultSet resultset)
		throws IOException;
	
	/**
	 * Re-name the specified query.  query.setName() should <em>not</em>
	 * be called before using the method!
	 * 
	 * @param project
	 * @param query
	 * @param newName
	 * 
	 * @throws IOException if the query folder could not be re-named.
	 */
	public abstract void renameQuery(Project project, Query query, String newName)
		throws IOException;
	
	
}
