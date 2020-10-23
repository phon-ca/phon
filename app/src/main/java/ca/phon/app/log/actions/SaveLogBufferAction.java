/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.log.actions;

import java.awt.event.*;
import java.io.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.toast.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;

public class SaveLogBufferAction extends HookableAction {
	
	private static final long serialVersionUID = -2827879669257916438L;
	
	private final LogBuffer logBuffer;
	
	private final static String CMD_NAME = "Save";
	
	private final static String SHORT_DESC = "Save buffer to file...";
	
	private final static String ICON = "actions/document-save-as";

	public SaveLogBufferAction(LogBuffer buffer) {
		this.logBuffer = buffer;
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(logBuffer != null) {
			final SaveDialogProperties props = new SaveDialogProperties();
			props.setParentWindow(CommonModuleFrame.getCurrentFrame());
			props.setRunAsync(true);
			props.setCanCreateDirectories(true);
			props.setTitle("Save buffer: "  + logBuffer.getBufferName());
			props.setInitialFile(logBuffer.getBufferName() + ".txt");
			
			props.setListener( (e) -> {
				if(e.getDialogData() != null) {
					final String saveAs = e.getDialogData().toString();
					PhonWorker.getInstance().invokeLater(() -> saveBuffer(saveAs));
				}
			});
			
			NativeDialogs.showSaveDialog(props);
		}
	}

	private void saveBuffer(String saveAs) {
		try {
			final FileOutputStream out = new FileOutputStream(new File(saveAs));
			final OutputStreamWriter writer = new OutputStreamWriter(out, logBuffer.getEncoding());
			writer.write(logBuffer.getText());
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			LogUtil.severe(ex.getLocalizedMessage(), ex);
			ToastFactory.makeToast(ex.getLocalizedMessage()).start(logBuffer);
		}
	}
	
}
