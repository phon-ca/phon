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
package ca.phon.app.opgraph.wizard.actions;

import ca.phon.app.excel.WorkbookUtils;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.*;
import ca.phon.app.opgraph.report.tree.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.*;
import jxl.Workbook;
import jxl.write.*;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;

public class SaveTablesToWorkbookAction extends HookableAction {

	private ReportTree reportTree;

	private WritableWorkbook workbook;
	
	public SaveTablesToWorkbookAction(ReportTree reportTree) {
		super();
		
		this.reportTree = reportTree;
	}
	
	public boolean exportReportNode(String filename, ReportTreeNode node, boolean useIntegerForBoolean) {
		if(node instanceof TableNode) {
			return exportTable(filename, node, useIntegerForBoolean);
		} else if(node instanceof ExcelExportableNode) {
			return exportExcelNode(filename, node);
		} else 
			return false;
	}

	public boolean exportExcelNode(String filename, ReportTreeNode node) {
		final ExcelExportableNode excelNode = (ExcelExportableNode)node;
		
		final ExcelExporter exporter = excelNode.getExporter();
		final File workbookFile = new File(filename);
		if(workbook == null) {
			try {
				workbook = Workbook.createWorkbook(workbookFile);
			} catch (IOException e) {
				LogUtil.severe(e);
				return false;
			}
		}
		try {
			exporter.addToWorkbook(workbook);
		} catch (WriteException e) {
			LogUtil.severe(e);
			return false;
		}
		
		return true;
	}

	public boolean exportTable(String filename, ReportTreeNode node, boolean useIntegerForBoolean) {
		if(!(node instanceof TableNode)) return false;
		final TableNode tableNode = (TableNode)node;
		
		final DefaultTableDataSource table = (DefaultTableDataSource)tableNode.getTable();
		ReportTreePath treePath = tableNode.getPath();
		// remove root from path and use path as filename
		treePath = treePath.pathByRemovingRoot();
		
		final File workbookFile = new File(filename);
		if(workbook == null) {
			try {
				workbook = Workbook.createWorkbook(workbookFile);
			} catch (IOException e) {
				LogUtil.severe(e);
				return false;
			}
		}
		
		String tableId = treePath.toString();
		String name = WorkbookUtils.sanitizeTabName(tableId);
		
		int attempts = 1;
		while(List.of(workbook.getSheetNames()).contains(name)) {
			name = WorkbookUtils.sanitizeTabName(tableId + " (" + (attempts++) + ")");
		}
		
		final WritableSheet sheet = workbook.createSheet(name, workbook.getNumberOfSheets());
		try {
			WorkbookUtils.addTableToSheet(sheet, 0, table, useIntegerForBoolean);
		} catch (WriteException e1) {
			LogUtil.severe(e1);
			return false;
		}
		
		return true;
	}
	
	public void done(List<ReportTreeNode> processedNodes) {
		if(workbook != null) {
			try {
				workbook.write();
				workbook.close();
			} catch (IOException | WriteException e) {
				LogUtil.severe(e);
			}
		}
	}
	
	public String getFilename() {
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.excelFilter);
		props.setInitialFile("Report." + FileFilter.excelFilter.getDefaultExtension());
		props.setRunAsync(false);

		final String saveAs = NativeDialogs.showSaveDialog(props);
		return saveAs;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ReportTableExportDialog exportDialog = new ReportTableExportDialog(this.reportTree, this::getFilename, this::exportReportNode, this::done, true);
		exportDialog.setParentFrame(CommonModuleFrame.getCurrentFrame());
		exportDialog.showDialog();
	}

}
