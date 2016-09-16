/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.KeyStroke;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.project.OpenProjectEP;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class OpenProjectCommand extends HookableAction {

	private static final long serialVersionUID = 4170522090398409697L;
	
	private final static String TXT = "Open project...";
	
	private final static String DESC = "Browse for project...";
	
	private final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);

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
				final HashMap<String, Object> initInfo = new HashMap<String, Object>();
				initInfo.put(OpenProjectEP.PROJECTPATH_PROPERTY, projectPath);
				
				PluginEntryPointRunner.executePluginInBackground(OpenProjectEP.EP_NAME, initInfo);
			}
		}
		
	};
	
}
