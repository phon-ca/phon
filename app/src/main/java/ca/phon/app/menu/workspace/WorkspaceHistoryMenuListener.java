package ca.phon.app.menu.workspace;

import java.io.File;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.workspace.Workspace;
import ca.phon.app.workspace.WorkspaceHistory;
import ca.phon.ui.action.PhonUIAction;

public class WorkspaceHistoryMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		final WorkspaceHistory history = new WorkspaceHistory();
		for(File workspaceFolder:history) {
			final PhonUIAction workspaceFolderAct = new PhonUIAction(Workspace.class, "setUserWorkspaceFolder", workspaceFolder);
			workspaceFolderAct.putValue(PhonUIAction.NAME, workspaceFolder.getAbsolutePath());
			workspaceFolderAct.putValue(PhonUIAction.SHORT_DESCRIPTION, workspaceFolder.getAbsolutePath());
			menu.add(workspaceFolderAct);
		}
		
		menu.addSeparator();
		final PhonUIAction clearHistoryAct = new PhonUIAction(history, "clearHistory");
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear workspace history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear workspace folder history");
		menu.add(clearHistoryAct);
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
