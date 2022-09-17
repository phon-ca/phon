/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import java.util.*;

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