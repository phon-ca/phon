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
package ca.phon.app.query.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.file.OpenFileHistory;
import ca.phon.query.script.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Export query to a file on disk.
 */
public class ExportQueryAction extends HookableAction {
	
	private QueryScript queryScript;
	
	private String initialFolder;
	
	private String queryName;
	
	public ExportQueryAction(QueryScript queryScript, String initialFolder, String queryName) {
		super();
		
		this.queryScript = queryScript;
		this.initialFolder = initialFolder;
		this.queryName = queryName;
	
		final ImageIcon saveIcon = IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
		putValue(SMALL_ICON, saveIcon);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// attempt to create target folder if it doesn't exist
		if(initialFolder != null) {
			final File folder = new File(initialFolder);
			if(!folder.exists()) {
				folder.mkdirs();
			}
			if(!folder.isDirectory()) {
				LogUtil.warning("Target path could not be created or is not a folder");
				initialFolder = null;
			}
		}
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(initialFolder);
		
		if(queryName != null) {
			props.setInitialFile(queryName);
		}
		props.setNameFieldLabel("Query name");
		props.setPrompt("Save Query");
		props.setRunAsync(true);
		props.setListener(this::dialogClosed);
		
		NativeDialogs.showSaveDialog(props);
	}

	public void dialogClosed(NativeDialogEvent evt) {
		if(evt.getDialogResult() != NativeDialogEvent.OK_OPTION) return;
		
		String saveAs = evt.getDialogData().toString();
		try {
			QueryScriptLibrary.saveScriptToFile(queryScript, saveAs);
			
			final OpenFileHistory openFileHistory = new OpenFileHistory();
			openFileHistory.addToHistory(new File(saveAs));
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}
	
}
