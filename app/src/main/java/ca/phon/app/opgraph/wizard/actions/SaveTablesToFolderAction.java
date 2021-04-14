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

import java.awt.event.*;
import java.io.*;
import java.lang.Boolean;
import java.util.*;

import javax.swing.*;

import org.jdesktop.swingx.*;

import au.com.bytecode.opencsv.*;
import ca.phon.app.excel.*;
import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.app.opgraph.report.tree.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.query.report.datasource.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.*;
import ca.phon.util.OSInfo;
import jxl.*;
import jxl.write.*;

public class SaveTablesToFolderAction extends HookableAction {
	
	private final static String EXPORT_WITH_FOLDERS_PROP = SaveTablesToFolderAction.class.getName() + ".exportWithFolders";
	private final static boolean DEFAULT_EXPORT_WITH_FOLDERS = Boolean.TRUE;
	private boolean exportWithFolders = PrefHelper.getBoolean(EXPORT_WITH_FOLDERS_PROP, DEFAULT_EXPORT_WITH_FOLDERS);
	
	private JCheckBox exportWithFoldersBox;
	
	private final NodeWizard wizard;
	
	private final static String TXT = "Export tables to folder ";
		
	public static enum ExportType {
		CSV,
		EXCEL
	};
	private final ExportType type;
	
	public SaveTablesToFolderAction(NodeWizard wizard, ExportType type) {
		super();
		
		this.type = type;
		this.wizard = wizard;
		
		putValue(HookableAction.NAME, TXT);
	}
	
	public ExportType getType() {
		return this.type;
	}
	
	public boolean exportReportNode(String folder, ReportTreeNode node, boolean useIntegerForBoolean) {
		if(node instanceof TableNode) {
			return exportTable(folder, node, useIntegerForBoolean);
		} else if(node instanceof ExcelExportableNode) {
			return exportExcelNode(folder, node, useIntegerForBoolean);
		} else 
			return false;
	}

	public boolean exportExcelNode(String folder, ReportTreeNode node, boolean useIntegerForBoolean) {
		final ExcelExportableNode excelNode = (ExcelExportableNode)node;
		
		final ExcelExporter exporter = excelNode.getExporter();

		final File tableFile = getFileForNode(folder, node);
		final File parentFolder = tableFile.getParentFile();
		
		if(!parentFolder.exists()) {
			parentFolder.mkdirs();
		}
		if(!parentFolder.isDirectory()) {
			return false;
		}
		
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(tableFile);
			exporter.addToWorkbook(workbook);
			workbook.write();
			workbook.close();
		} catch (WriteException | IOException e) {
			LogUtil.severe(e);
			return false;
		}
		
		return true;
	}
	
	public boolean exportTable(String folder, ReportTreeNode node, boolean useIntegerForBoolean) {
		if(!(node instanceof TableNode)) return false;
		final TableNode tableNode = (TableNode)node;
		
		final DefaultTableDataSource table = (DefaultTableDataSource)tableNode.getTable();

		final File tableFile = getFileForNode(folder, tableNode);
		final File parentFolder = tableFile.getParentFile();
		
		if(!parentFolder.exists()) {
			parentFolder.mkdirs();
		}
		if(!parentFolder.isDirectory()) {
			return false;
		}
		
		try {
			writeTableToFile(table, tableFile, "UTF-8", useIntegerForBoolean);
			return true;
		} catch (IOException e) {
			LogUtil.severe(e);
			return false;
		}
	}
	
	private File getFileForNode(String folder, ReportTreeNode tableNode) {
		ReportTreePath treePath = tableNode.getPath();
		// remove root from path and use path as filename
		treePath = treePath.pathByRemovingRoot();
		
		final String illegalCharRegex = "[\\\\/\\[\\]*?:]";
		String subPath = File.separator;
		for(int i = 0; i < treePath.getPath().length; i++) {
			ReportTreeNode ele = treePath.getPath()[i];
			if(i > 0)
				subPath += (exportWithFolders ? File.separator : "_");
			subPath += ele.getTitle().trim().replaceAll(illegalCharRegex, "_");
		}
		final String tableFilePath = folder + subPath + (type == ExportType.CSV ? ".csv" : ".xls");
		
		final File tableFile = new File(tableFilePath);
		
		return tableFile;
	}
	
	public void done(List<ReportTreeNode> processedNodes) {
		
	}
	
	public String getFolder() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setCanCreateDirectories(true);
		props.setPrompt("Save to folder");
		props.setNameFieldLabel("Report folder:");
		
		final List<String> retVal = NativeDialogs.showOpenDialog(props);
		return (retVal != null && retVal.size() > 0 ? retVal.get(0) : null);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final BufferPanel reportBuffer = wizard.getBufferPanel().getBuffer("Report");
		if(reportBuffer != null) {
			final ReportTree tree = (ReportTree)reportBuffer.getUserObject();
			
			exportWithFoldersBox = new JCheckBox("Create subfolders as shown");
			exportWithFoldersBox.setSelected(exportWithFolders);
			
			final ActionListener l = (e) -> {
				exportWithFolders = exportWithFoldersBox.isSelected();
				PrefHelper.getUserPreferences().putBoolean(EXPORT_WITH_FOLDERS_PROP, exportWithFolders);
			};
			exportWithFoldersBox.addActionListener(l);
			
			final ReportTableExportDialog exportDialog = new ReportTableExportDialog(tree, this::getFolder, this::exportReportNode, this::done, getType() == ExportType.EXCEL ? true : false);
			exportDialog.setParentFrame(wizard);

			exportDialog.getCustomOptionsPanel().add(exportWithFoldersBox);
			
			exportDialog.showDialog();
		}
	}
	
	private void writeTableToFile(DefaultTableDataSource table, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
		switch(getType() ) {
		case CSV:
			writeTableToCSVFile(table, file, encoding, useIntegerForBoolean);
			break;
			
		case EXCEL:
			writeTableToExcelWorkbook(table, file, encoding, useIntegerForBoolean);
			break;
			
		default:
			break;
		}
	}
	
	private void writeTableToExcelWorkbook(DefaultTableDataSource table, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
		final WritableWorkbook workbook = Workbook.createWorkbook(file);
		final WritableSheet sheet = workbook.createSheet("Sheet 1", 1);
		
		try {
			WorkbookUtils.addTableToSheet(sheet, 0, table, useIntegerForBoolean);
			workbook.write();
		} catch (WriteException e) {
			throw new IOException(e);
		} finally {
			try {
				workbook.close();
			} catch (WriteException e) {
				LogUtil.severe(e);
			}
		}		
	}
	
	private void writeTableToCSVFile(DefaultTableDataSource table, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
		final CSVWriter writer = 
				new CSVWriter(new PrintWriter(file, encoding), ',', '\"', 
						(OSInfo.isWindows() ? "\r\n" : "\n"));
		
		// write column header
		final String[] colnames = new String[table.getColumnCount()];
		for(int i = 0; i < table.getColumnCount(); i++) {
			colnames[i] = table.getColumnTitle(i);
		}
		writer.writeNext(colnames);
		
		final String[] currentRow = new String[table.getColumnCount()];
		for(int row = 0; row < table.getRowCount(); row++) {
			for(int col = 0; col < table.getColumnCount(); col++) {
				Object cellVal = table.getValueAt(row, col);
				if(cellVal instanceof Boolean && useIntegerForBoolean) {
					cellVal = (cellVal == Boolean.TRUE ? 1 : 0);
				}
				currentRow[col] = (cellVal == null ? "" : cellVal.toString());
			}
			writer.writeNext(currentRow);
		}
		writer.flush();
		writer.close();
	}
	
}
