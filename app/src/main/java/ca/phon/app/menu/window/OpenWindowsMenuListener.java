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
package ca.phon.app.menu.window;

import java.awt.Window;
import java.lang.ref.WeakReference;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

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
