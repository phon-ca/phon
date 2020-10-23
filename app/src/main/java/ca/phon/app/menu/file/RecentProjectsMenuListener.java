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

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.modules.*;
import ca.phon.app.project.*;
import ca.phon.plugin.*;
import ca.phon.ui.action.*;

/**
 * Setup recent projects menu
 */
public class RecentProjectsMenuListener implements MenuListener {

	@Override
	public void menuCanceled(MenuEvent arg0) {
		
	}

	@Override
	public void menuDeselected(MenuEvent arg0) {
		
	}

	@Override
	public void menuSelected(MenuEvent arg0) {
		JMenu menu = (JMenu)arg0.getSource();
		menu.removeAll();
		
		final RecentProjects history = new RecentProjects();
		for(File projectFile:history) {
			PluginAction projectAct = new PluginAction("OpenProject");
			final EntryPointArgs args = new EntryPointArgs();
			args.put(EntryPointArgs.PROJECT_LOCATION, projectFile.getAbsolutePath());
			projectAct.putArgs(args);
			projectAct.putValue(Action.NAME, projectFile.getAbsolutePath());
			projectAct.putValue(Action.SHORT_DESCRIPTION, projectFile.getAbsolutePath());
			
			JMenuItem projectItem = new JMenuItem(projectAct);
			menu.add(projectItem);
		}
		
		menu.addSeparator();
		final PhonUIAction clearHistoryAct = new PhonUIAction(history, "clearHistory");
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear project history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear recent project history");
		menu.add(clearHistoryAct);
	}
	
}
