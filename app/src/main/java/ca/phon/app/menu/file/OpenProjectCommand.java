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
package ca.phon.app.menu.file;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ca.phon.app.hooks.*;
import ca.phon.app.modules.*;
import ca.phon.app.project.*;
import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;

public class OpenProjectCommand extends HookableAction {

	private static final long serialVersionUID = 4170522090398409697L;
	
	private final static String TXT = "Open project...";
	
	private final static String DESC = "Browse for project...";
	
	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK);

	public OpenProjectCommand() {
		super();
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setAllowMultipleSelection(false);
		props.setCanCreateDirectories(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setTitle("Open Project");
		props.setListener(openProjectListener);
	//	props.setInitialFolder(Workspace.userWorkspace().getWorkspaceFolder().getAbsolutePath());
		NativeDialogs.showOpenDialog(props);
	}

	private final NativeDialogListener openProjectListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			final String projectPath = (String)event.getDialogData();
			if(projectPath != null) {
				final EntryPointArgs args = new EntryPointArgs();
				args.put(EntryPointArgs.PROJECT_LOCATION, projectPath);
				
				PluginEntryPointRunner.executePluginInBackground(OpenProjectEP.EP_NAME, args);
			}
		}
		
	};
	
}
