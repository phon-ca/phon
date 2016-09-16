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

import ca.phon.project.Project;

/**
 * A factory interface for the creation of query-related objects. This
 * interface exists to allow for plugins to specify their own implementations
 * for the basic query information used in Phon.
 */
public interface QueryFactory {
	
	/**
	 * Create a query instance without an attached project.
	 * This type of query is used to save a script along with
	 * entered parameters.
	 * 
	 * @return a Query instance
	 */
	public abstract Query createQuery();
	
	/**
	 * Create a Query instance for the specified project.
	 * 
	 * @param project  the project this query will be created for
	 * 
	 * @return a Query instance
	 */
	public abstract Query createQuery(Project project);
	
	/**
	 * Create a Script instance.
	 * 
	 * @return a Script instance
	 */
	public abstract Script createScript();
	
	/**
	 * Create a ResultSet instance.
	 * 
	 * @return a ResultSet instance
	 */
	public abstract ResultSet createResultSet();

	/**
	 * Create a Result instance.
	 * 
	 * @return a Result instance
	 */
	public abstract Result createResult();
	
	/**
	 * Create a ResultValue instance.
	 * 
	 * @return a ResultValue instance
	 */
	public abstract ResultValue createResultValue();
}
