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
package ca.phon.app;

/**
 * Actions performed when the application version changes.
 * 
 */
public interface VersionTrigger {
	
	/**
	 * Fired when executing a new version of the application for the first time.
	 * This method may be called when the version increases or 
	 * decreases.
	 * 
	 * @param prevVersion
	 * @param currentVersion
	 */
	public void versionChanged(String prevVersion, String currentVersion);
	
}
