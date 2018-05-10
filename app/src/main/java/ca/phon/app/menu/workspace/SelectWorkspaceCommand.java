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
package ca.phon.app.menu.workspace;

import java.awt.event.ActionEvent;
import java.io.File;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.workspace.Workspace;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;

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
