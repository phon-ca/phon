package ca.phon.app.menu.window;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.plugin.PluginAction;
import ca.phon.ui.CommonModuleFrame;

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
