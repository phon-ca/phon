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

import ca.phon.app.BootWindow;
import ca.phon.plugin.PluginException;

/**
 * Interface used to perform operations before Phon is
 * opened. This hook will always be called when Phon 
 * starts, unlike {@link PhonBootHook} which is only 
 * called when using the {@link BootWindow}.
 * 
 * {@link PhonStartupHook}s are executed just before
 * the first module is called.  A use case for this class
 * is to change the execution flow of the application.
 */
public interface PhonStartupHook {

	/**
	 * Perform whatever operations are necessary for
	 * plug-in startup.
	 * 
	 * @throws PluginException on error.  Excpetions
	 * will be printed to the logger
	 */
	public void startup() throws PluginException;
	
}
