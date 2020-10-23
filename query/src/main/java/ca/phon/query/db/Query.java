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

import java.time.*;
import java.util.*;

/**
 * Interface for a Phon query. 
 */
public interface Query {
	/**
	 * Gets the name of this query.
	 * 
	 * @return the name
	 */
	public abstract String getName();
	
	/**
	 * Sets the name of this query.
	 * 
	 * @param name  the name
	 */
	public abstract void setName(String name);
	
	/**
	 * Gets the UUID used to uniquely identify this query.
	 * 
	 * @return the uuid
	 */
	public abstract UUID getUUID();
	
	/**
	 * Sets the UUID used to uniquely identify this query.
	 * 
	 * @param uuid  the uuid
	 */
	public abstract void setUUID(UUID uuid);

	/**
	 * Gets the date of creation of this query.
	 * 
	 * @return the date
	 */
	public abstract LocalDateTime getDate();
	
	/**
	 * Sets the date of creation of this query.
	 * 
	 * @param date  the date
	 */
	public abstract void setDate(LocalDateTime date);

	/**
	 * Gets whether or not this query is starred.
	 * 
	 * @return the starred state of this query 
	 */
	public abstract boolean isStarred();

	/**
	 * Sets the starred status of this query.
	 * 
	 * @param starred  the starred state for this query
	 */
	public abstract void setStarred(boolean starred);

	/**
	 * Gets the script for this query.
	 * 
	 * @return the script 
	 */
	public abstract Script getScript();

	/**
	 * Sets the script for this query.
	 * 
	 * @param starred  the starred state for this query
	 */
	public abstract void setScript(Script script);
	
	/**
	 * Gets the tags for this query.
	 * 
	 * @return the list of tags
	 */
	public abstract List<String> getTags();
	
	/**
	 * Gets the comments for this query.
	 * 
	 * @return the list of comments
	 */
	public abstract String getComments();
	
	/**
	 * Sets comments for this query
	 * 
	 * @param comments
	 */
	public abstract void setComments(String comments);
}