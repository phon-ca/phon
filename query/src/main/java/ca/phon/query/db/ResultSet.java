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

import java.util.Iterator;

/**
 * Interface for a set of results from a Phon query.
 *
 */
public interface ResultSet extends Iterable<Result> {
	/**
	 * Gets the corpus name for this result set.
	 * 
	 * @return the corpus name 
	 */
	public abstract String getCorpus();

	/**
	 * Gets the session name for this result set.
	 * 
	 * @return the session name 
	 */
	public abstract String getSession();
	
	/**
	 * Gets the session path for this result set. The session path should be
	 * of the form <tt>[corpus].[session]</tt>
	 * 
	 * @return the session path
	 */
	public abstract String getSessionPath();
	
	/**
	 * Sets the session path for this result set. The session path should be
	 * of the form <tt>[corpus].[session]</tt>
	 * 
	 * @param sessionPath  the session path
	 */
	public abstract void setSessionPath(String sessionPath);
	
	/**
	 * Sets the session path for this result set. The session path will be
	 * of the form <tt>[corpus].[session]</tt>
	 * 
	 * @param sessionPath  the session path
	 */
	public abstract void setSessionPath(String corpus, String session);

	/**
	 * Get the number of results in this set.
	 * 
	 * @return the size of the result set
	 */
	public abstract int size();
	
	/**
	 * Get the result specified by the given index.
	 * 
	 * @param idx
	 * @return the result for the given index
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public abstract Result getResult(int idx);
	
	/**
	 * Remove the result specified by the given index.
	 * 
	 * @param idx
	 * @return the removed result
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public abstract Result removeResult(int idx);
	
	/**
	 * Add the given result to the result set
	 * 
	 * @param result
	 */
	public abstract void addResult(Result res);
	
	/**
	 * Returns the number of results in this set.
	 * 
	 * @param includeExcluded include excluded results
	 *  in the set.  If <code>true</code> this will
	 *  return this same value as {@link #getResults()}.size()
	 * 
	 * @return the number of (non-excluded)
	 *  results in this set
	 */
	public abstract int numberOfResults(boolean includeExcluded);
	
	/**
	 * Return the list of metadata keys used in the result set.
	 * 
	 * @return the list of metadata keys
	 */
	public abstract String[] getMetadataKeys();
	
	/**
	 * Return an iterator for results
	 * 
	 * @param includeExcluded
	 * @return iterator
	 */
	public abstract Iterator<Result> iterator(boolean includeExcluded);
	
	/**
	 * Return an iterator for all results in this set.
	 * This is the same as calling iterator(true).
	 * 
	 * 
	 */
	public abstract Iterator<Result> iterator();
	
}