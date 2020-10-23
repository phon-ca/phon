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
