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

import java.util.*;

/**
 * Interface for the result of a Phon query. 
 */
public interface Result extends Iterable<ResultValue> {
	/**
	 * Gets the record index for this result.
	 * 
	 * @return the record index
	 */
	public abstract int getRecordIndex();

	/**
	 * Sets the record index for this result.
	 * 
	 * @param index  the record index
	 */
	public abstract void setRecordIndex(int index);

	/**
	 * Gets the map of metadata for this result. 
	 * 
	 * @return the metadata map 
	 */
	public abstract Map<String, String> getMetadata();

	/**
	 * Gets the schema of this result.
	 *       
	 * @return the schema
	 */
	public abstract String getSchema();

	/**
	 * Sets the schema of this result.
	 * 
	 * @param schema  the format 
	 */
	public abstract void setSchema(String schema);
	
	/**
	 * Is this result excluded from reports
	 * 
	 * @return <code>true</code> if this result should be excluded
	 *  <code>false</code> otherwise.
	 */
	public abstract boolean isExcluded();
	
	/**
	 * Set the excluded status of this result.
	 * 
	 * @param excluded
	 */
	public abstract void setExcluded(boolean excluded);
	
	/**
	 * Get the number of result values.
	 * 
	 * @return number of result values
	 */
	public abstract int getNumberOfResultValues();
	
	/**
	 * Get the result value by name
	 * 
	 * @param name
	 * @return result value optional
	 */
	public abstract Optional<ResultValue> getResultValue(String name);
	
	/**
	 * Get the specified result value
	 * 
	 * @param idx
	 * @return result value
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public abstract ResultValue getResultValue(int idx);
	
	/**
	 * Remove the specified result value
	 * 
	 * @param idx
	 * @return the removed result value
	 */
	public abstract ResultValue removeResultValue(int idx);
	
	/**
	 * Add the given result value.
	 * 
	 * @param resultValue
	 * @return index of added result value
	 */
	public abstract int addResultValue(ResultValue resultValue);
	
}
