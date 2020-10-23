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
package ca.phon.app.menu.workspace;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.modules.*;
import ca.phon.app.project.*;
import ca.phon.app.workspace.*;
import ca.phon.plugin.*;
import ca.phon.project.*;

public class WorkspaceProjectsMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		final JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		final Workspace workspace = Workspace.userWorkspace();
		for(Project project:workspace.getProjects()) {
			final String projectPath = project.getLocation();
			final EntryPointArgs args = new EntryPointArgs();
			args.put(EntryPointArgs.PROJECT_LOCATION, projectPath);
			
			final PluginAction act = new PluginAction(OpenProjectEP.EP_NAME, true);
			act.putValue(PluginAction.NAME, project.getName());
			act.putValue(PluginAction.SHORT_DESCRIPTION, project.getLocation());
			act.putArgs(args);
			menu.add(act);
		}
	}

	@Override
	public void menuDeselected(MenuEvent e) {

	}

	@Override
	public void menuCanceled(MenuEvent e) {
	
	}

}
