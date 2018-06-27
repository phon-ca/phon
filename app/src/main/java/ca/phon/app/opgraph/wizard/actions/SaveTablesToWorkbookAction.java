package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JCheckBox;

import org.jdesktop.swingx.VerticalLayout;

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
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction.ExportType;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class SaveTablesToWorkbookAction extends HookableAction {

	private final NodeWizard wizard;
	
	private WritableWorkbook workbook;
	
	public SaveTablesToWorkbookAction(NodeWizard wizard) {
		super();
		
		this.wizard = wizard;
	}

	public boolean exportTable(String filename, ReportTreeNode node) {
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
			WorkbookUtils.addTableToSheet(sheet, 0, table);
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
		final BufferPanel reportBuffer = wizard.getBufferPanel().getBuffer("Report");
		if(reportBuffer != null) {
			final ReportTree tree = (ReportTree)reportBuffer.getUserObject();
			
			final ReportTableExportDialog exportDialog = new ReportTableExportDialog(tree, this::getFilename, this::exportTable, this::done);
			exportDialog.setParentFrame(wizard);
			
			exportDialog.showDialog();
		}
	}
	
	
}
