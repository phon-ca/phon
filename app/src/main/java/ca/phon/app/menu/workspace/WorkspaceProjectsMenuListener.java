package ca.phon.app.menu.workspace;

import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.project.OpenProjectEP;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;
import ca.phon.workspace.Workspace;

public class WorkspaceProjectsMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		final JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		final Workspace workspace = Workspace.userWorkspace();
		for(Project project:workspace.getProjects()) {
			final String projectPath = project.getLocation();
			final HashMap<String, Object> initInfo = new HashMap<String, Object>();
			initInfo.put(OpenProjectEP.PROJECTPATH_PROPERTY, projectPath);
			
			final PluginAction act = new PluginAction(OpenProjectEP.EP_NAME, true);
			act.putValue(PluginAction.NAME, project.getName());
			act.putValue(PluginAction.SHORT_DESCRIPTION, project.getLocation());
			act.putArgs(initInfo);
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
