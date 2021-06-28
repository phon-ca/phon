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

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.DesktopProjectFactory;
import ca.phon.app.project.OpenProjectEP;
import ca.phon.app.welcome.WelcomeWindow;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.ProjectFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class NewProjectCommand extends HookableAction {

private final static String TXT = "New project...";

	private final static String DESC = "Create project...";

	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_N,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK);

	private static final long serialVersionUID = -1288974377105467180L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(NewProjectCommand.class.getName());

	public NewProjectCommand() {
		super();
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setTitle("New Project");
		props.setMessage("Choose path and enter the name of the new project below.");
		props.setNameFieldLabel("Project name:");
		props.setPrompt("Create Project");
		props.setRunAsync(true);

		props.setInitialFolder(Workspace.userWorkspaceFolder().getAbsolutePath());
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				final String folderName = e.getDialogData().toString();
				final File folder = new File(folderName);
				
				PhonWorker.getInstance().invokeLater( () -> createProject(folder) );
			}
		});
		NativeDialogs.showDialog(props);
	}
	
	// executed on worker thread
	private void createProject(File folder) {
		if(folder.exists()) {
			final MessageDialogProperties p2 = new MessageDialogProperties();
			p2.setParentWindow(CommonModuleFrame.getCurrentFrame());
			p2.setHeader("Create Project Failed");
			try {
				p2.setMessage("Cannot create project, an item already exists at path " + folder.getCanonicalPath());
			} catch (IOException e) {
			}
			p2.setOptions(MessageDialogProperties.okOptions);
			p2.setRunAsync(true);
			NativeDialogs.showMessageDialog(p2);

			return;
		}

		final File parentFolder = folder.getParentFile();
		final File testFile = new File(parentFolder, "project.xml");
		if(testFile.exists()) {
			final MessageDialogProperties p2 = new MessageDialogProperties();
			p2.setParentWindow(CommonModuleFrame.getCurrentFrame());
			p2.setHeader("Create Project Failed");
			p2.setMessage("Cannot create project inside of another");
			p2.setOptions(MessageDialogProperties.okOptions);
			p2.setRunAsync(true);
			NativeDialogs.showMessageDialog(p2);

			return;
		}

		try {
			final ProjectFactory factory = new DesktopProjectFactory();
			factory.createProject(folder);

			// open project
			final EntryPointArgs args = new EntryPointArgs();
			args.put(EntryPointArgs.PROJECT_LOCATION, folder.getCanonicalPath());

			PluginEntryPointRunner.executePluginInBackground(OpenProjectEP.EP_NAME, args);

			final File workspaceFolder = Workspace.userWorkspaceFolder();
			if(workspaceFolder.equals(parentFolder)) {
				// update welcome window (if visible)
				for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
					if(cmf instanceof WelcomeWindow) {
						((WelcomeWindow)cmf).refreshWorkspaceProjects();
					}
				}
			}
		} catch (IOException ex) {
			LOGGER.error( ex.getLocalizedMessage(), ex);
			final MessageDialogProperties p2 = new MessageDialogProperties();
			p2.setParentWindow(CommonModuleFrame.getCurrentFrame());
			p2.setHeader("Create Project Failed");
			p2.setMessage("Cannot create project: " + ex.getLocalizedMessage());
			p2.setOptions(MessageDialogProperties.okOptions);
			p2.setRunAsync(true);
			NativeDialogs.showMessageDialog(p2);
		}
	}

}
