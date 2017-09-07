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

import java.time.LocalDateTime;
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