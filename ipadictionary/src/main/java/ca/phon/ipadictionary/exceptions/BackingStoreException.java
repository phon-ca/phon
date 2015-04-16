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
package ca.phon.ipadictionary.exceptions;

/**
 * Exceptions thrown when problems are encountered with the
 * dictionary backing storage (e.g., I/O Errors.)
 * 
 * 
 */
public class BackingStoreException extends IPADictionaryExecption {

	public BackingStoreException() {
		super();
	}

	public BackingStoreException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BackingStoreException(String arg0) {
		super(arg0);
	}

	public BackingStoreException(Throwable arg0) {
		super(arg0);
	}
	
}
