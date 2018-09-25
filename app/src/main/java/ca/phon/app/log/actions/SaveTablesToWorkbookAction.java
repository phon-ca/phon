package ca.phon.app.log.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferPanelContainer;
import ca.phon.app.log.LogUtil;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.worker.PhonWorker;
import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

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
