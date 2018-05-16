package ca.phon.app.opgraph.wizard.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.derby.impl.load.Export;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.WorkbookUtils;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.query.report.csv.CSVTableDataWriter;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.OSInfo;
import ca.phon.worker.PhonWorker;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SaveTablesToFolderAction extends HookableAction {
	
	private final NodeWizard wizard;
	
	private final static String TXT = "Save tables to folder ";
		
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

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(wizard);
		props.setRunAsync(true);
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setCanCreateDirectories(true);
		props.setPrompt("Save to folder");
		props.setNameFieldLabel("Report folder:");
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				final String path = e.getDialogData().toString();
				PhonWorker.getInstance().invokeLater( () -> {
					try {
						saveTablesToFolder(path);
					} catch (IOException e1) {
						Toolkit.getDefaultToolkit().beep();
						wizard.showErrorMessage(e1.getLocalizedMessage());
						LogUtil.severe(e1.getMessage());
					}
				} );
			}
		});
		NativeDialogs.showOpenDialog(props);
	}
	
	private void saveTablesToFolder(String path) throws IOException {
		final BufferPanel reportBuffer = wizard.getBufferPanel().getBuffer("Report");
		if(reportBuffer != null) {
			final ReportTree tree = (ReportTree)reportBuffer.getUserObject();
			final Map<String, DefaultTableDataSource> tableMap = new LinkedHashMap<>();
			wizard.searchForTables(tree.getRoot(), tableMap);

			final File outputFolder = new File(path);
			if(!outputFolder.exists()) {
				outputFolder.mkdirs();
			}
			
			for(String tableId:tableMap.keySet()) {
				final DefaultTableDataSource table = tableMap.get(tableId);
				
				final String illegalCharRegex = "[\\\\/\\[\\]*?:]";
				String initialFilename = tableId.trim() + (getType() == ExportType.CSV ? ".csv" : ".xls");
				initialFilename = initialFilename.replaceAll(illegalCharRegex, "_");
				
				final File tableFile = new File(outputFolder, initialFilename);
				writeTableToFile(table, tableFile, "UTF-8");
			}
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	private void writeTableToFile(DefaultTableDataSource table, File file, String encoding) throws IOException {
		switch(getType() ) {
		case CSV:
			writeTableToCSVFile(table, file, encoding);
			break;
			
		case EXCEL:
			writeTableToExcelWorkbook(table, file, encoding);
			break;
			
		default:
			break;
		}
	}
	
	private void writeTableToExcelWorkbook(DefaultTableDataSource table, File file, String encoding) throws IOException {
		final WritableWorkbook workbook = Workbook.createWorkbook(file);
		final WritableSheet sheet = workbook.createSheet("Sheet 1", 1);
		
		try {
			WorkbookUtils.addTableToSheet(sheet, 0, table);
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
	
	private void writeTableToCSVFile(DefaultTableDataSource table, File file, String encoding) throws IOException {
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
				final Object cellVal = table.getValueAt(row, col);
				currentRow[col] = (cellVal == null ? "" : cellVal.toString());
			}
			writer.writeNext(currentRow);
		}
		writer.flush();
		writer.close();
	}
	
}
