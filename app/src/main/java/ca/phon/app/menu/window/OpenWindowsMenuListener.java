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
package ca.phon.app.menu.window;

import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.OpenProjectEP;
import ca.phon.app.project.ProjectWindow;
import ca.phon.app.welcome.WelcomeWindow;
import ca.phon.app.welcome.WelcomeWindowEP;
import ca.phon.plugin.PluginAction;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		
		WelcomeWindow welcomeWindow = null;
		
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
			
			if(cmf instanceof WelcomeWindow) {
				welcomeWindow = (WelcomeWindow)cmf;
			}
		}
		
		for(Project project:projectWindows.keySet()) {
			final JMenu projectMenu = new JMenu(project.getName());
			boolean projectWindowAvail = false;
			for(CommonModuleFrame projectWindow:projectWindows.get(project)) {
				if(projectWindow instanceof ProjectWindow)
					projectWindowAvail = true;
				final PhonUIAction<Void> showWindowAct = PhonUIAction.runnable(projectWindow::toFront);
				showWindowAct.putValue(PhonUIAction.NAME, projectWindow.getTitle());
				showWindowAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Bring window to front");
				final JMenuItem projectWindowItem = new JMenuItem(showWindowAct);
				projectMenu.add(projectWindowItem);
			}
			if(!projectWindowAvail) {
				PhonUIAction<Project> showProjectWindowAct = PhonUIAction.eventConsumer(OpenWindowsMenuListener::openProjectWindow, project);
				showProjectWindowAct.putValue(PhonUIAction.NAME, "Show project window");
				showProjectWindowAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show project window");
				menu.add(showProjectWindowAct);
			}
			menu.add(projectMenu);
		}
		
		for(CommonModuleFrame cmf:strayWindows) {
			final PhonUIAction<Void> showWindowAct = PhonUIAction.runnable(cmf::toFront);
			showWindowAct.putValue(PhonUIAction.NAME, cmf.getTitle());
			showWindowAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Bring window to front");
			final JMenuItem projectWindowItem = new JMenuItem(showWindowAct);
			menu.add(projectWindowItem);
		}
		
		// if no welcome window found, add item to show it
		if(welcomeWindow == null) {
			final PluginAction welcomeWindowAct = new PluginAction(WelcomeWindowEP.EP_NAME);
			welcomeWindowAct.putValue(PhonUIAction.NAME, "Show Welcome window");
			menu.add(new JMenuItem(welcomeWindowAct));
		}
		
		// generic close item
		final JMenuItem closeItem = new JMenuItem(new CloseWindowCommand(owner.get()));
		menu.add(closeItem);
	}
	
	public static void openProjectWindow(PhonActionEvent<Project> pae) {
		Project project = (Project)pae.getData();
		
		EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_OBJECT, project);
		try {
			PluginEntryPointRunner.executePlugin(OpenProjectEP.EP_NAME, args);
		} catch (PluginException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}

}
