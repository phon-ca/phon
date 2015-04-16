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
package ca.phon.extensions;

import java.util.Set;

/**
 * Adds the ability to add/remove capabilites to an
 * object that implements the ICapable interface.
 * 
 * 
 */
public interface IExtendable {

	/**
	 * Return all extension types supported
	 * 
	 */
	public Set<Class<?>> getExtensions();

	/**
	 * Get the requested extension if available.
	 * 
	 * @param class of the requested capability
	 * @return the capability object or <code>null</code> if
	 *  the cability is not available
	 */
	public <T> T getExtension(Class<T> cap);
	
	/**
	 * Add a new extension.
	 * 
	 * @param cap the extension to add
	 * @return the added extension implementation
	 */
	public <T> T putExtension(Class<T> cap, T impl);
	
	/**
	 * Remove a capability.
	 * 
	 * @param cap the capability to remove
	 */
	public <T> T removeExtension(Class<T> cap);
	
}
