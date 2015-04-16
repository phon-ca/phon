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
package ca.phon.plugin;

import java.util.Map;

/**
 * Entry point for a plugin
 */
public interface IPluginEntryPoint {
	
	/**
	 * Plugin action ID.
	 * 
	 * This is used by Phon to call the
	 * entry point using a string name.
	 */
	public String getName();
	
	/**
	 * Entry point method for a plugin.
	 * 
	 * @param args a hash table of arguments given to the 
	 * plugin.  By default, Phon will provide the plugin with
	 * the following arguments:
	 * 
	 * project:IPhonProject - the project.  null if N/A.  
	 * corpus:String - the corpus id of the open session. null if N/A
	 * session:String - the session id of the open session. null if N/A
	 * 
	 * More arguments can be defined statically in the module
	 * definition xml files.
	 * 
	 */
	public void pluginStart(Map<String, Object> args);
	

}
