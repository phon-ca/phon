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
