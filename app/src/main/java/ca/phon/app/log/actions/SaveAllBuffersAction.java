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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferPanelContainer;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

public class SaveAllBuffersAction extends HookableAction {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SaveAllBuffersAction.class.getName());

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
			final String selectedFolder = (String)e.getDialogData();
			if(selectedFolder != null) {
				PhonWorker.getInstance().invokeLater(() -> saveBuffers(selectedFolder));
			}
		});
		NativeDialogs.showOpenDialog(props);
	}

	private void saveBuffers(String saveFolder) {
		for(String bufferName:buffers.getBufferNames()) {
			final BufferPanel bufferPanel = buffers.getBuffer(bufferName);
			final File bufferFile = new File(saveFolder, bufferName + "." + bufferPanel.getDefaultExtension());
			save(bufferPanel, bufferFile.getAbsolutePath());
		}
		
		if( ((MultiBufferPanel)buffers).isOpenAfterSaving() ) {
			try {
				OpenFileLauncher.openURL((new File(saveFolder)).toURI().toURL());
			} catch (MalformedURLException e) {
				LOGGER.warn( e.getLocalizedMessage(), e);
			}
		}
	}
	
	private void save(BufferPanel panel, String saveAs) {
		if(panel == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
		try {
			if(panel.isShowingBuffer()) {
				panel.writeToTextFile(saveAs, "UTF-8");
			} else if(panel.isShowingTable()) {
				panel.writeToCSV(saveAs, "UTF-8");
			} else if(panel.isShowingHtml()) {
				panel.writeHMTLFile(saveAs, "UTF-8");
			}
		} catch (IOException ex) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(ex);
		}
	}
	
}
