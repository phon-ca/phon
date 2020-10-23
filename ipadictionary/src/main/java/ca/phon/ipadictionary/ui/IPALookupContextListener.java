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
package ca.phon.ipadictionary.ui;

/**
 * Listener interface for IPALookupContext
 *
 */
public interface IPALookupContextListener {

	/**
	 * Fired when the selected dictionary changes.
	 * 
	 */
	public void dictionaryChanged(String newDictionary);
	
	/**
	 * Fired when a new dictionary is added.
	 * 
	 */
	public void dictionaryAdded(String newDictionary);
	
	/**
	 * Fired when a dictionary is dropped
	 */
	public void dictionaryRemoved(String dictName);

	/**
	 * Fired when a new messages is available from
	 * the lookup context.
	 */
	public void handleMessage(String msg);

	/**
	 * Fired when a new error occurs
	 */
	public void errorOccured(String err);
	
}
