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
 * Exception thrown when a requested capability is not
 * implemented in the dictionary object.
 * 
 */
public class CapabilityNotImplemented extends IPADictionaryExecption {
	
	/**
	 * Requested capability
	 */
	private Class<?> capability;

	public CapabilityNotImplemented(Class<?> cap) {
		super();
		this.capability = cap;
	}

	public CapabilityNotImplemented(Class<?> cap, String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.capability = cap;
	}

	public CapabilityNotImplemented(Class<?> cap, String arg0) {
		super(arg0);
		this.capability = cap;
	}

	public CapabilityNotImplemented(Class<?> cap, Throwable arg0) {
		super(arg0);
		this.capability = cap;
	}
	
	

}
