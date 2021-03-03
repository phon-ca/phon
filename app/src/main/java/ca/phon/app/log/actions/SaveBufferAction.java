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
package ca.phon.app.log.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class SaveBufferAction extends HookableAction {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SaveLogBufferAction.class.getName());

	private static final long serialVersionUID = -2827879669257916438L;

	private final static String CMD_NAME = "Save...";

	private final static String SHORT_DESC = "Save buffer to file";

	private final static String ICON = "actions/document-save-as";

	private final MultiBufferPanel container;

	private final String bufferName;

	public SaveBufferAction(MultiBufferPanel bufferPanel) {
		this(bufferPanel, null);
	}

	public SaveBufferAction(MultiBufferPanel bufferPanel, String bufferName) {
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));

		this.bufferName = bufferName;
		this.container = bufferPanel;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferPanel panel =
				(this.bufferName == null ? this.container.getCurrentBuffer()
						: this.container.getBuffer(bufferName));
		if(panel == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);

		FileFilter filter = null;
		if(panel.isShowingBuffer() == true) {
			filter = new FileFilter("Text file", "txt");
		} else if(panel.isShowingHtml() == true) {
			filter = new FileFilter("HTML Files (*.html;*.htm)", "html;htm");
		} else if(panel.isShowingTable()) {
			filter = FileFilter.csvFilter;
		}
		props.setFileFilter(filter);

		final String illegalCharRegex = "[\\\\/\\[\\]*?:]";
		String initialFilename = panel.getBufferName();
		initialFilename = initialFilename.trim().replaceAll(illegalCharRegex, "_");
		if(initialFilename.endsWith("." + filter.getDefaultExtension())) {
			initialFilename = initialFilename.substring(0, initialFilename.length()-(filter.getDefaultExtension().length()+1));
		}
		
		props.setInitialFile(initialFilename + "." + filter.getDefaultExtension());
		props.setRunAsync(true);
		props.setListener( (e) -> {
			if(e.getDialogResult() == NativeDialogEvent.OK_OPTION && e.getDialogData() != null) {
				final String saveAs = e.getDialogData().toString();
				PhonWorker.getInstance().invokeLater( () -> save(saveAs) );
			}
		});

		NativeDialogs.showSaveDialog(props);
	}
	
	private void save(String saveAs) {
		final BufferPanel panel =
				(this.bufferName == null ? this.container.getCurrentBuffer()
						: this.container.getBuffer(bufferName));
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

			if(this.container.isOpenAfterSaving() && Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(new File(saveAs));
			}
		} catch (IOException ex) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(ex);
		}
	}

}
