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
package ca.phon.session.check;

/**
 * Quick fix for session checks.  Subclasses need to implement the fix() method.
 *
 */
public abstract class SessionQuickFix {
	
	private final ValidationEvent validationEvent;
	
	public SessionQuickFix(ValidationEvent validationEvent) {
		super();
		
		this.validationEvent = validationEvent;
	}
	
	public ValidationEvent getValidationEvent() {
		return validationEvent;
	}
	
	public String getDescription() {
		return "Fix issue";
	}

	/**
	 * Perform the quick fix operation.
	 * 
	 * @return <code>true</code> if the fix was successful
	 */
	public abstract boolean fix();
	
}
