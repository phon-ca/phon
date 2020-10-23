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
package ca.phon.worker;

import java.util.*;

/**
 *
 */
public interface PhonTaskListener extends EventListener {
	
	/**
	 * Sent when the running status of the task
	 * has changed.
	 * 
	 * @param task
	 * @param oldStatus
	 * @param newStatus
	 */
	public void 
		statusChanged(PhonTask task, PhonTask.TaskStatus oldStatus, 
				PhonTask.TaskStatus newStatus);
	
	/**
	 * Sent when a property of the task changes.  Subclasses
	 * of PhonTask can define additional properties.
	 * 
	 * @param task
	 * @param propName
	 * @param oldValue
	 * @param newValue
	 */
	public void
		propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue);
	
}
