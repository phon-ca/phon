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
package ca.phon.session.spi;

public interface TierSPI<T>  extends TierDescriptionSPI {
	
	public boolean isGrouped();
	
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
	public T removeGroup(int idx);
	
	/**
	 * Removes all group data from this tier and sets
	 * the number of groups to 0
	 * 
	 */
	public void removeAll();

}
