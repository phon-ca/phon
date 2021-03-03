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

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.OpenProjectEP;
import ca.phon.app.workspace.Workspace;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

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
