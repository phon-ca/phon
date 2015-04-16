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

import java.util.List;
import java.util.Map;

import ca.phon.app.BootWindow;

/**
 * Interface used to perform operations before Phon is
 * started using the {@link BootWindow}.
 * 
 * These hooks are called just before the application is
 * executed and allow modification of the ProcessBuilder
 * and command.
 * 
 */
public interface PhonBootHook {

	/**
	 * Modify/add to a list of vmoptions for the application.
	 * 
	 * @param vmopts
	 * 
	 */
	public void setupVMOptions(List<String> vmopts);
	
	/**
	 * Modify/add to environment for the application.
	 * 
	 * @param environment
	 */
	public void setupEnvironment(Map<String, String> environment);
	
}