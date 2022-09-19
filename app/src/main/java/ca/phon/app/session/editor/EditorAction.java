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

package ca.phon.app.session.editor;

/**
 * Interface for handling editor actions.
 */
public interface EditorAction {

	/**
	 * Called when an editor event occurs.
	 *
	 * This method is always called on the
	 * editor event queue (not the AWT event queue.)
	 */
	public void eventOccurred(EditorEvent ee);

}
