package ca.phon.app.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;

public class ShowApplicationDataFolderCommand extends HookableAction {

	public ShowApplicationDataFolderCommand() {
		super();
		
		putValue(HookableAction.NAME, "Show application data folder");
		putValue(HookableAction.SHORT_DESCRIPTION, PrefHelper.getUserDataFolder());
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final File userDataFolder = new File(PrefHelper.getUserDataFolder());
		try {
			OpenFileLauncher.openURL(userDataFolder.toURI().toURL());
		} catch (MalformedURLException e) {
			LogUtil.severe(e.getLocalizedMessage(), e);
		}
	}

}
