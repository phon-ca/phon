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
package ca.phon.ipadictionary.spi;

import java.util.Iterator;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Metadata consists of a map of string to string
 * values.
 */
@Extension(IPADictionary.class)
public interface Metadata {
	
	/**
	 * Get value for a given metadata key.
	 * 
	 * @param key the metadata key.  Common keys are
	 *  'provider' and 'website'
	 * @return the value for the specified key or <code>null</code>
	 *  if no data is available. See {@link #metadataKeyIterator()}
	 */
	public String getMetadataValue(String key);
	
	/**
	 * Get the iteator for metadata keys.
	 * 
	 * @return an iterator for the metadata keys available
	 */
	public Iterator<String> metadataKeyIterator();

}
