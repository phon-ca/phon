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
package ca.phon.app.hooks;

import ca.phon.plugin.PluginException;

/**
 * Interface used to perform operations before Phon is
 * shutdown.
 * 
 * Hooks are called just before System.exit - after
 * the save modified dialog is presented.
 */
public interface PhonShutdownHook {

	/**
	 * Perform whatever operations are necessary for
	 * plug-in cleanup.
	 * 
	 * @throws PluginException on error.  Excpetions
	 * will be printed to the logger
	 */
	public void shutdown() throws PluginException;
	
}
