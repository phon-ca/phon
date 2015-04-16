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
package ca.phon.app.menu.window;

import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Populates a menu with open windows organized by project.
 */
public class OpenWindowsMenuListener implements MenuListener {
	
	private final Window owner;
	
	public OpenWindowsMenuListener(Window window) {
		super();
		this.owner = window;
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
		
//		// add windows sorted by open project
//		// get open projects
//		IPhonProject openProjects[] = 
//			PhonEnvironment.getInstance().getOpenProjects().toArray(new IPhonProject[0]);
//		
//		CommonModuleFrame openWindows[] = 
//			CommonModuleFrame.getOpenWindows().toArray(new CommonModuleFrame[0]);
//		for(IPhonProject proj:openProjects) {
//			String projName = new String();
//				projName = 
//					proj.getProjectName();
//			if(menu.getItemCount() > 0) 
//				menu.addSeparator();
////			JMenu projMenu = new JMenu(projName);
////			JMenuItem projItem = new JMenuItem(projName);
////			projItem.setEnabled(false);
////			menu.add(projItem);
//				
//			for(CommonModuleFrame f:openWindows) {
//				if(!f.isShowInWindowMenu() || !f.isShowing())
//					continue;
//				
//				if(f.getProject() != null
//						&& f.getProject() == proj) {
//					JMenuItem windowItem = new JMenuItem(f.getTitle());
//					
//					windowItem.addActionListener(new ActionListener() {
//
//						@Override
//						public void actionPerformed(ActionEvent arg0) {
//							for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
//								if(f.getTitle().equals( ((JMenuItem)arg0.getSource()).getText())) {
//									
//									f.toFront();
////									CommonModuleFrame.currentFrame = f;
//								}
//							}
//						}
//						
//					});
//					menu.add(windowItem);
//				}
//			}
////			menu.add(projMenu);
//		}
//		
//		menu.addSeparator();
//		
//		for(CommonModuleFrame f:openWindows) {
//			
//			if(f.getProject() != null || f.getParentFrame() != null)
//				continue;
//			
//			if(!f.isShowInWindowMenu() || !f.isShowing())
//				continue;
//			
//			JMenuItem windowItem = new JMenuItem(f.getTitle());
//			windowItem.addActionListener(new ActionListener() {
//
//				@Override
//				public void actionPerformed(ActionEvent arg0) {
//					for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
//						if(f.getTitle().equals( ((JMenuItem)arg0.getSource()).getText())) {
//							f.toFront();
////							CommonModuleFrame.currentFrame = f;
//						}
//					}
//				}
//				
//			});
//			
//			menu.add(windowItem);
//		}
//		
//		menu.addSeparator();
		
		// generic close item
		final JMenuItem closeItem = new JMenuItem(new CloseWindowCommand(owner));
		menu.add(closeItem);
	}

}
