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
package ca.phon.app.menu.window;

import java.awt.Window;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;

/**
 * Populates a menu with open windows organized by project.
 */
public class OpenWindowsMenuListener implements MenuListener {
	
	private final WeakReference<Window> owner;
	
	public OpenWindowsMenuListener(Window window) {
		super();
		this.owner = new WeakReference<Window>(window);
	}

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
		
		final Map<Project, List<CommonModuleFrame>> projectWindows = 
				new LinkedHashMap<>();
		final List<CommonModuleFrame> strayWindows = new ArrayList<>();
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			final Project project = cmf.getExtension(Project.class);
			if(project == null) {
				strayWindows.add(cmf);
			} else {
				List<CommonModuleFrame> windows = projectWindows.get(project);
				if(windows == null) {
					windows = new ArrayList<>();
					projectWindows.put(project, windows);
				}
				windows.add(cmf);
			}
		}
		
		for(Project project:projectWindows.keySet()) {
			final JMenu projectMenu = new JMenu(project.getName());
			for(CommonModuleFrame projectWindow:projectWindows.get(project)) {
				final PhonUIAction showWindowAct = new PhonUIAction(projectWindow, "toFront");
				showWindowAct.putValue(PhonUIAction.NAME, projectWindow.getTitle());
				showWindowAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Bring window to front");
				final JMenuItem projectWindowItem = new JMenuItem(showWindowAct);
				projectMenu.add(projectWindowItem);
			}
			menu.add(projectMenu);
		}
		
		for(CommonModuleFrame cmf:strayWindows) {
			final PhonUIAction showWindowAct = new PhonUIAction(cmf, "toFront");
			showWindowAct.putValue(PhonUIAction.NAME, cmf.getTitle());
			showWindowAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Bring window to front");
			final JMenuItem projectWindowItem = new JMenuItem(showWindowAct);
			menu.add(projectWindowItem);
		}
		
		// generic close item
		final JMenuItem closeItem = new JMenuItem(new CloseWindowCommand(owner.get()));
		menu.add(closeItem);
	}

}
