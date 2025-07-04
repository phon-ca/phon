/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.menu.file;

import ca.phon.app.actions.OpenFileEP;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.*;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;

public class RecentFilesMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		OpenFileHistory fileHistory = new OpenFileHistory();
		for(File file:fileHistory) {
			PhonUIAction<File> openFileAct = PhonUIAction.consumer(this::openFile, file);
			openFileAct.putValue(PhonUIAction.NAME, file.getAbsolutePath());
			menu.add(openFileAct);
		}
		
		if(fileHistory.size() > 0) {
			menu.addSeparator();
		}
		
		PhonUIAction<Void> clearHistoryAct = PhonUIAction.runnable(fileHistory::clearHistory);
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
