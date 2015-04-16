/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

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
		
//		// add recent projects to menu
//		SystemProperties recentProjects = 
//			   UserPrefManager.getUserRecentProjects();
//		   
//		for(int i = 1; i <= 5; i++) {
//			final String projectString =
//				recentProjects.getProperty("ca.phon.project.recent."+i).toString();
//			   
//			if(projectString.length() > 0) {
//				PluginAction projectAct = new PluginAction("OpenProject");
//				projectAct.putArg("ca.phon.modules.core.OpenProjectController.projectpath", projectString);
//				projectAct.putValue(Action.NAME, projectString);
//				projectAct.putValue(Action.SHORT_DESCRIPTION, "Open project");
//				
//				JMenuItem projectItem = new JMenuItem(projectAct);
//				menu.add(projectItem);
//			}
//			
//		}
	}
	
}
