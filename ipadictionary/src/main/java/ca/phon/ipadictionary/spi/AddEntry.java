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
package ca.phon.ipadictionary.spi;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;

/**
 * IPADictionary capability for adding a new
 * orthography->ipa entry.
 *
 */
@Extension(IPADictionary.class)
public interface AddEntry {
	
	/**
	 * Add a new entry to the ipa dictionary
	 * 
	 * @param orthography
	 * @param ipa
	 * @throws IPADictionaryExecption if the entry was
	 *  not added to the dictionary.  E.g., the key->value
	 *  pair already exists or the dictionary was not able
	 *  to add the entry to it's storage.
	 */
	public void addEntry(String orthography, String ipa)
		throws IPADictionaryExecption;
	
}
