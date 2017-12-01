package ca.phon.app.log.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.*;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.*;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import jxl.Workbook;
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
				try {
					WritableWorkbook workbook = saveTablesToWorkbook(buffers, saveAs);
					workbook.close();
				} catch (IOException | WriteException ex) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(ex);
				}
			}
		});
		NativeDialogs.showSaveDialog(props);
	}
	
	public WritableWorkbook saveTablesToWorkbook(BufferPanelContainer buffers, String saveAs) 
			throws IOException, WriteException {
		final WritableWorkbook retVal = Workbook.createWorkbook(new File(saveAs));
		
		for(String bufferName:buffers.getBufferNames()) {
			final BufferPanel bp = buffers.getBuffer(bufferName);
			if(bp.isShowingTable()) {
				bp.createSheetInExcelWorkbook(retVal);
			}
		}
		
		retVal.write();
		return retVal;
	}

}
