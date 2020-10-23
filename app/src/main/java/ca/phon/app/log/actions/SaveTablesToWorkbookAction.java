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

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.query.report.datasource.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.worker.*;
import jxl.*;
import jxl.write.*;

/**
 * Save all buffers with {@link TableDataSource} user objects to a new
 * JExcel workbook
 * 
 */
public class SaveTablesToWorkbookAction extends HookableAction {

	private BufferPanelContainer buffers;
	
	private final static String TXT = "Save tables to Excel\u2122 wookbook";
	private final static String DESC = "Save all tables an Excel\\u2122 workbook...";
	
	public SaveTablesToWorkbookAction(BufferPanelContainer buffers) {
		super();
		
		putValue(HookableAction.NAME, TXT);
		putValue(HookableAction.SHORT_DESCRIPTION, DESC);
		
		this.buffers = buffers;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setFileFilter(FileFilter.excelFilter);
		props.setCanCreateDirectories(true);
		props.setInitialFile("Untitled.xls");
		props.setMessage("Save tables to workbook");
		props.setTitle("Save tables to workbook");
		props.setRunAsync(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setListener( (e) -> {
			if(e.getDialogResult() == NativeDialogEvent.OK_OPTION
					&& e.getDialogData() != null) {
				final String saveAs = e.getDialogData().toString();
				PhonWorker.getInstance().invokeLater( () -> saveTablesToWorkbook(buffers, saveAs) );
			}
		});
		NativeDialogs.showSaveDialog(props);
	}
	
	public void saveTablesToWorkbook(BufferPanelContainer buffers, String saveAs) {
		try {
			final WritableWorkbook retVal = Workbook.createWorkbook(new File(saveAs));
			
			for(String bufferName:buffers.getBufferNames()) {
				final BufferPanel bp = buffers.getBuffer(bufferName);
				if(bp.isShowingTable()) {
					bp.createSheetInExcelWorkbook(retVal);
				}
			}
			
			retVal.write();
			retVal.close();
		} catch (IOException | WriteException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}

}
