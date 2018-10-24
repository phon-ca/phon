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

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferPanelContainer;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

public class SaveAllBuffersAction extends HookableAction {

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
			if(Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().open(new File(saveFolder));
				} catch (IOException e) {
					LogUtil.warning(e);
				}
			}
//			try {
//				OpenFileLauncher.openURL((new File(saveFolder)).toURI().toURL());
//			} catch (MalformedURLException e) {
//				LOGGER.warn( e.getLocalizedMessage(), e);
//			}
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
