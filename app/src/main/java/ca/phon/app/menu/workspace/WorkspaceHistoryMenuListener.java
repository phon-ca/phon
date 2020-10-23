package ca.phon.app.menu.workspace;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.workspace.*;
import ca.phon.ui.action.*;

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
