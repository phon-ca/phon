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

import ca.phon.phonex.PhonexMatcher;
import ca.phon.util.Range;

import java.util.regex.Matcher;

/**
 * A reference to a piece of data in a result.
 */
public interface ResultValue {

	/**
	 * Get the name for this result value
	 *
	 * @return name if not specified this will be
	 *  the same as the tier name
	 */
	public abstract String getName();

	/**
	 * Set the name for this result value
	 *
	 * @param name
	 */
	public abstract void setName(String name);

	/**
	 * Gets the tier name for this result value.
	 *
	 * @return the tier name
	 */
	public abstract String getTierName();

	/**
	 * Sets the tier name for this result value.
	 *
	 * @param tierName  the tier name
	 */
	public abstract void setTierName(String tierName);
	
	/**
	 * Gets the range for this result value.
	 *
	 * @return the range
	 */
	public abstract Range getRange();

	/**
	 * Sets the range for this result value.
	 *
	 * @param range  the range
	 */
	public abstract void setRange(Range range);

	/**
	 * Gets the group index for this result value.
	 *
	 * @return the group index
	 */
	public abstract int getGroupIndex();

	/**
	 * Sets the group index for this result value.
	 *
	 * @param groupIndex  the group index
	 */
	public abstract void setGroupIndex(int groupIndex);

	/**
	 * Gets the data for this result value.
	 *
	 * @return the data
	 */
	public abstract String getData();

	/**
	 * Sets the data for this result value.
	 *
	 * @param data  the data
	 */
	public abstract void setData(String data);

	/**
	 * Returns the number of 'matcher' groups either that were produced
	 * by either regex {@link Matcher}s or {@link PhonexMatcher}s.
	 *
	 * @return number of matcher groups
	 */
	public abstract int getMatcherGroupCount();

	/**
	 * Get the value of the specified matcher group.
	 *
	 * @param index
	 *
	 * @return value of the specified group
	 *
	 */
	public abstract String getMatcherGroup(int index);

}