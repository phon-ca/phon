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
