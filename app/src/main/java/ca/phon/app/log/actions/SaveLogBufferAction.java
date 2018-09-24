/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.log.LogUtil;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

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
