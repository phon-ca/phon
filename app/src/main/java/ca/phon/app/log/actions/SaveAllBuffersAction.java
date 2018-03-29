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
import java.io.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.ImageIcon;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.*;
import ca.phon.worker.PhonWorker;

public class SaveAllBuffersAction extends HookableAction {

	private final static Logger LOGGER = Logger.getLogger(SaveAllBuffersAction.class.getName());

	private final static String TXT = "Save all buffers to folder...";

	private final static String DESC = "Save all buffers to a selected folder";

	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/save_all", IconSize.SMALL);

	private BufferPanelContainer buffers;

	public SaveAllBuffersAction(BufferPanelContainer bufferPanelContainer) {
		super();

		this.buffers = bufferPanelContainer;

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setCanCreateDirectories(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setTitle("Select Buffers to Folder");
		props.setInitialFolder(PrefHelper.getUserDataFolder());
		props.setPrompt("Save Buffers to Folder");
		props.setRunAsync(true);
		props.setListener( (e) -> {
			@SuppressWarnings("unchecked")
			final List<String> selectedFolders = (List<String>)e.getDialogData();
			if(selectedFolders != null && selectedFolders.size() > 0) {
				final String folder = selectedFolders.get(0);
				PhonWorker.getInstance().invokeLater(() -> saveBuffers(folder));
			}
		});
		NativeDialogs.showSaveDialog(props);
	}

	private void saveBuffers(String saveFolder) {
		for(String bufferName:buffers.getBufferNames()) {
			final BufferPanel bufferPanel = buffers.getBuffer(bufferName);
			final LogBuffer logBuffer = bufferPanel.getLogBuffer();
			final File bufferFile = new File(saveFolder, bufferName + "." + bufferPanel.getDefaultExtension());
			try {
				final FileOutputStream out = new FileOutputStream(bufferFile);
				final OutputStreamWriter writer = new OutputStreamWriter(out, logBuffer.getEncoding());
				writer.write(logBuffer.getText());
				writer.flush();
				writer.close();
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
				ToastFactory.makeToast(ex.getLocalizedMessage()).start(logBuffer);
			}
		}
	}
	
}
