package ca.phon.app.menu.file;

import java.awt.Toolkit;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.actions.OpenFileEP;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.ipa.PhoneMapDisplayUI;

public class RecentFilesMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		OpenFileHistory fileHistory = new OpenFileHistory();
		for(File file:fileHistory) {
			PhonUIAction openFileAct = new PhonUIAction(this, "openFile", file);
			openFileAct.putValue(PhonUIAction.NAME, file.getAbsolutePath());
			menu.add(openFileAct);
		}
		
		if(fileHistory.size() > 0) {
			menu.addSeparator();
		}
		
		PhonUIAction clearHistoryAct = new PhonUIAction(fileHistory, "clearHistory");
		clearHistoryAct.putValue(PhonUIAction.NAME, "Clear file history");
		clearHistoryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear open file history");
		menu.add(clearHistoryAct);
	}
	
	public void openFile(File file) {
		EntryPointArgs args = new EntryPointArgs();
		args.put(OpenFileEP.INPUT_FILE, file);
		try {
			PluginEntryPointRunner.executePlugin(OpenFileEP.EP_NAME, args);
		} catch (PluginException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
