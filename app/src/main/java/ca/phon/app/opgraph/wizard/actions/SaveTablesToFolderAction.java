package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JCheckBox;

import org.jdesktop.swingx.VerticalLayout;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.WorkbookUtils;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.app.opgraph.report.tree.ReportTreePath;
import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.ReportTableExportDialog;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

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
	
	public boolean exportTable(String folder, ReportTreeNode node) {
		if(!(node instanceof TableNode)) return false;
		final TableNode tableNode = (TableNode)node;
		
		final DefaultTableDataSource table = (DefaultTableDataSource)tableNode.getTable();
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
		final File parentFolder = tableFile.getParentFile();
		
		if(!parentFolder.exists()) {
			parentFolder.mkdirs();
		}
		if(!parentFolder.isDirectory()) {
			return false;
		}
		
		try {
			writeTableToFile(table, tableFile, "UTF-8");
			return true;
		} catch (IOException e) {
			LogUtil.severe(e);
			return false;
		}
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
			
			final ReportTableExportDialog exportDialog = new ReportTableExportDialog(tree, this::getFolder, this::exportTable, this::done);
			exportDialog.setParentFrame(wizard);
			
			exportDialog.getCustomOptionsPanel().setLayout(new VerticalLayout());
			exportDialog.getCustomOptionsPanel().add(exportWithFoldersBox);
			
			exportDialog.showDialog();
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
