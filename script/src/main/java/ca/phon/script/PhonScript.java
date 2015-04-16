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
package ca.phon.script;

import java.net.URI;
import java.util.List;

import ca.phon.extensions.IExtendable;

/**
 * <p>Interface for Phon runtime scripts.  Scripts are written in
 * ECMAScript and use the Rhino engine directly (instead of using
 * the JSR.)</p>
 * 
 * <p>Phon scripts may also have parameters defined which can be
 * setup using either a comment at the beginning of the file or
 * by implementing the <code>setup_params</code> function in the 
 * script.</p>
 * 
 */
public interface PhonScript extends IExtendable {
	
	/**
	 * Get the script text.
	 * 
	 * @return script
	 */
	public String getScript();
	
	/**
	 * Get a script context for this script.  The context
	 * is used to compile and evaulate the script.
	 * 
	 * @return the script context
	 */
	public PhonScriptContext getContext();
	
	/**
	 * Get required packages that should be imported when
	 * the scope is created.  These packages will also be
	 * available to any script imported using the <code>require()</code>
	 * function.
	 * 
	 * @return the list of packages that should be available
	 *  to this script and any dependencies
	 */
	public List<String> getPackageImports();
	
	/**
	 * Get a list of classes that should be imported when
	 * the scope is created.  These classes will also
	 * be availble to any script imported using the <code>require()</code>
	 * function.
	 * 
	 * @return the list of classes that should be availble
	 *  to this script and any dependencies
	 */
	public List<String> getClassImports();
	
	/**
	 * Get the list of URLs that should be available
	 * for script loading using the <code>require</code>
	 * function.
	 * 
	 * @return list of javascript library folders
	 */
	public List<URI> getRequirePaths();
}
