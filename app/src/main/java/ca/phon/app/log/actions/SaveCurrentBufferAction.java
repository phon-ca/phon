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
import java.io.*;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.icons.*;

public class SaveCurrentBufferAction extends HookableAction {
	
	private static final Logger LOGGER = Logger
			.getLogger(SaveLogBufferAction.class.getName());
	
	private static final long serialVersionUID = -2827879669257916438L;
	
	private final static String CMD_NAME = "Save buffer...";
	
	private final static String SHORT_DESC = "Save buffer to file";
	
	private final static String ICON = "actions/document-save-as";
	
	private final MultiBufferPanel container;

	public SaveCurrentBufferAction(MultiBufferPanel bufferPanel) {
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		
		this.container = bufferPanel;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferPanel panel = this.container.getCurrentBuffer();
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		
		FileFilter filter = null;
		if(panel.isShowingBuffer() == true) {
			filter = new FileFilter("Text file", "txt");
		} else if(panel.isShowingHtml() == true) {
			filter = FileFilter.htmlFilter;
		} else if(panel.isShowingTable()) {
			filter = FileFilter.csvFilter;
		}
		props.setFileFilter(filter);
		
		props.setInitialFile(panel.getBufferName() + "." + filter.getDefaultExtension());
		props.setRunAsync(true);
		props.setListener( (e) -> {
			if(e.getDialogResult() == NativeDialogEvent.OK_OPTION && e.getDialogData() != null) {
				final String saveAs = e.getDialogData().toString();
				
				try {
					if(panel.isShowingBuffer() || panel.isShowingHtml()) {
						panel.writeToTextFile(saveAs, "UTF-8");
					} else if(panel.isShowingTable()) {
						panel.writeToCSV(saveAs, "UTF-8");
					}
					
					if(this.container.isOpenAfterSaving()) {
						OpenFileLauncher.openURL((new File(saveAs)).toURI().toURL());
					}					
				} catch (IOException ex) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(ex);
				}
			}
		});
		
		NativeDialogs.showSaveDialog(props);
	}

}
