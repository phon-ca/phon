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

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;

/**
 * Required interface for IPADictionary implementations.
 * IPADictionaries are expected to accept orthographic strings
 * and return one or more associated IPA transcriptions.
 * 
 */
public interface IPADictionarySPI {
	
	/**
	 * Lookup IPA transcriptions for a given
	 * orthographic string.
	 * 
	 * @param orthography
	 * @return a list of IPA transcriptions associated
	 *  with the given orthography
	 * @throws IPADictionary exception if an error occured
	 *  while attempting to lookup the given entry
	 */
	public String[] lookup(String orthography)
		throws IPADictionaryExecption;

	/**
	 * Install this SPI into the given IPADictionary
	 */
	public void install(IPADictionary dict);
}
