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
package ca.phon.project.exceptions;

/**
 * Exception 
 */
public class ProjectConfigurationException extends Exception {

	private static final long serialVersionUID = -615834580110057068L;

	public ProjectConfigurationException() {
		super();
	}

	public ProjectConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProjectConfigurationException(String message) {
		super(message);
	}

	public ProjectConfigurationException(Throwable cause) {
		super(cause);
	}
	
}
