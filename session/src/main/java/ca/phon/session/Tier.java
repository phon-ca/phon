/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.session;

import ca.phon.extensions.IExtendable;

/**
 * A tier in a record.  A tier has a name, type and a number
 * of groups.
 * 
 */
public interface Tier<T> extends TierDescription, Iterable<T>, IExtendable {
	
	/**
	 * Get the number of groups in the tier
	 * 
	 * @return number of groups
	 */
	public int numberOfGroups();
	
	/**
	 * Get value at given group
	 * 
	 * @param idx group indes
	 * @return value for the given group
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public T getGroup(int idx);
	
	/**
	 * Set the value of the specified group 
	 * idx.  idx must be between 0 and numberOfGroups()
	 * 
	 * @param idx
	 * @param val
	 * 
	 * @throws ArrayIndexOutOfBoundsException if idx is
	 *  out of bounds
	 * @throws NullPointerException if val is <code>null</code>
	 */
	public void setGroup(int idx, T val);
	
	/**
	 * Attempts to add a new group to the end of this tier
	 * and increments the number of groups.
	 * 
	 * 
	 */
	public void addGroup();
	
	/**
	 * Attempts to add a new group to the end of this tier
	 * and increments the number of groups.
	 * 
	 * @param idx
	 */
	public void addGroup(int idx);
	
	/**
	 * Adds a new group to the end of this tier and increments
	 * the number of groups.
	 * 
	 * @param val
	 */
	public void addGroup(T val);
	
	/**
	 * Adds a new group at the specified index
	 * 
	 * @param idx
	 * @param val
	 * 
	 * @throws ArrayIndexOutOfBoundsException if idx is
	 *  out of bounds
	 * @throws NullPointerException if val is <code>null</code>
	 */
	public void addGroup(int idx, T val);
	
	/**
	 * Remove the specified group
	 * 
	 * @param idx
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void removeGroup(int idx);
	
	/**
	 * Removes all group data from this tier and sets
	 * the number of groups to 0
	 * 
	 */
	public void removeAll();
	
	/**
	 * Add a tier listener
	 * 
	 * @param listener
	 */
	public void addTierListener(TierListener<T> listener);
	
	/**
	 * Remove the specified tier listener
	 * 
	 * @param listener
	 */
	public void removeTierListener(TierListener<T> listener);
	
}
