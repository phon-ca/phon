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

import ca.phon.app.workspace.*;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.event.*;
import java.io.File;

public class WorkspaceHistoryMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		final WorkspaceHistory history = new WorkspaceHistory();
		for(File workspaceFolder:history) {
			final PhonUIAction<File> workspaceFolderAct = PhonUIAction.consumer(Workspace::setUserWorkspaceFolder, workspaceFolder);
			workspaceFolderAct.putValue(PhonUIAction.NAME, workspaceFolder.getAbsolutePath());
			workspaceFolderAct.putValue(PhonUIAction.SHORT_DESCRIPTION, workspaceFolder.getAbsolutePath());
			menu.add(workspaceFolderAct);
		}
		
		menu.addSeparator();
		final PhonUIAction<Void> clearHistoryAct = PhonUIAction.runnable(history::clearHistory);
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear workspace history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear workspace folder history");
		menu.add(clearHistoryAct);
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
