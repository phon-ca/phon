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
package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Dictionary 'names.' The default name is the uri
 * of the loaded dictionary.
 *
 */
@Extension(IPADictionary.class)
public interface NameInfo {

	/**
	 * Returns a string identifier for this dictionary.
	 * While not required, the name should be unique
	 * to help users identify dictionaries which handle
	 * the same language.
	 * 
	 * @return the dictionary name
	 */
	public String getName();
	
}
