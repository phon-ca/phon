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
