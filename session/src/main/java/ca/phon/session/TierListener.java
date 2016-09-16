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
package ca.phon.session;

/**
 * Listener for tier group changes.
 */
public interface TierListener<T> {

	/**
	 * Called when a new group has been added to a tier
	 * 
	 * @param tier
	 * @param index
	 * @param value
	 */
	public void groupAdded(Tier<T> tier, int index, T value);
	
	/**
	 * Called when a group is removed from a tier
	 * 
	 * @param tier
	 * @param index
	 * @param value
	 */
	public void groupRemoved(Tier<T> tier, int index, T value);
	
	/**
	 * Called when the value of a group changes
	 * 
	 * @param tier
	 * @param index
	 * @param oldValue
	 * @param value
	 */
	public void groupChanged(Tier<T> tier, int index, T oldValue, T value);
	
	/**
	 * Called when all groups have been removed from a tier
	 * 
	 * @param tier
	 */
	public void groupsCleared(Tier<T> tier);
	
}
