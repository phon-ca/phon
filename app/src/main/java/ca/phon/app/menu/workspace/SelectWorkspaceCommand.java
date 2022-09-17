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
package ca.phon.app.menu.workspace;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.workspace.Workspace;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;

import java.awt.event.ActionEvent;
import java.io.File;

public class SelectWorkspaceCommand extends HookableAction {

	private static final long serialVersionUID = 6739632731206685754L;

	private final static String TXT = "Select Workspace folder...";
	private final static String DESC = "Select Workspace folder...";
	
	public SelectWorkspaceCommand() {
		super();
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setAllowMultipleSelection(false);
		props.setCanCreateDirectories(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setListener(workspaceFolderListener);
		props.setTitle("Select workspace folder");
		NativeDialogs.showOpenDialog(props);
	}
	
	private final NativeDialogListener workspaceFolderListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			final String selectedPath = (String)event.getDialogData();
			if(selectedPath != null) {
				final File workspaceFolder = new File(selectedPath);
				Workspace.setUserWorkspaceFolder(workspaceFolder);
			}
		}
		
	};

}
