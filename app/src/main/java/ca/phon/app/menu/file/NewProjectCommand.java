/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.menu.file;

import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.KeyStroke;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.*;
import ca.phon.app.welcome.WelcomeWindow;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.ProjectFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;
import ca.phon.worker.PhonWorker;

public class NewProjectCommand extends HookableAction {

private final static String TXT = "New project...";

	private final static String DESC = "Create project...";

	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_N,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);

	private static final long serialVersionUID = -1288974377105467180L;

	private final static Logger LOGGER = Logger.getLogger(NewProjectCommand.class.getName());

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
			LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
