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
package ca.phon.app.project.actions;

import java.lang.ref.*;

import ca.phon.app.hooks.*;
import ca.phon.app.project.*;
import ca.phon.ui.nativedialogs.*;

public abstract class ProjectWindowAction extends HookableAction {

	private static final long serialVersionUID = 7949135760405306345L;

	private final WeakReference<ProjectWindow> projectWindowRef;
	
	public ProjectWindowAction(ProjectWindow projectWindow) {
		super();
		this.projectWindowRef = new WeakReference<ProjectWindow>(projectWindow);
	}
	
	public ProjectWindow getWindow() {
		return this.projectWindowRef.get();
	}

	protected void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setTitle(msg1);
		props.setMessage(msg2);
		props.setParentWindow(getWindow());
		
		NativeDialogs.showDialog(props);
	}
	
}
