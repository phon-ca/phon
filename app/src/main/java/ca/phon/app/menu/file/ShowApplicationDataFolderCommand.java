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

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.util.PrefHelper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class ShowApplicationDataFolderCommand extends HookableAction {

	public ShowApplicationDataFolderCommand() {
		super();
		
		putValue(HookableAction.NAME, "Show application data folder");
		putValue(HookableAction.SHORT_DESCRIPTION, PrefHelper.getUserDataFolder());
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final File userDataFolder = new File(PrefHelper.getUserDataFolder());
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(userDataFolder);
			} catch (IOException e) {
				LogUtil.severe(e);
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

}
