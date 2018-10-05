/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
