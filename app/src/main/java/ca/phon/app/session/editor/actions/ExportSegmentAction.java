package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.CustomSegmentDialog;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.PlaySegmentAction.SegmentType;
import ca.phon.media.ExportSegment;
import ca.phon.media.LongSound;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.session.position.SegmentCalculator;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

public class ExportSegmentAction extends SessionEditorAction {

	public static enum SegmentType {
		CURRENT_RECORD,
		SPEAKER_TURN,
		CONVERSATION_PERIOD,
		CUSTOM
	};
	
	private SegmentType segmentType = SegmentType.CUSTOM;
	
	private long startTime = -1L;
	
	private long endTime = -1L;
	
	private String outputPath = null;

	public ExportSegmentAction(SessionEditor editor) {
		this(editor, SegmentType.CURRENT_RECORD);
		
		putValue(Action.NAME, "Export segment...");
		putValue(Action.SHORT_DESCRIPTION, "Export segment for current record");
//		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
	}
	
	public ExportSegmentAction(SessionEditor editor, SegmentType segmentType) {
		super(editor);
		
		this.segmentType = segmentType;
	}
	
	/**
	 * 
	 * @param editor
	 * @param startTime in s
	 * @param endTime in s
	 */
	public ExportSegmentAction(SessionEditor editor, float startTime, float endTime) {
		this(editor, startTime, endTime, null);
	}
	
	public ExportSegmentAction(SessionEditor editor, float startTime, float endTime, String outputPath) {
		this(editor, Float.valueOf(startTime * 1000.0f).longValue(), Float.valueOf(endTime * 1000.0f).longValue(), outputPath);
	}
	
	/**
	 * 
	 * @param editor
	 * @param startTime in ms
	 * @param endTime in ms
	 */
	public ExportSegmentAction(SessionEditor editor, long startTime, long endTime) {
		this(editor, startTime, endTime, null);
	}
	
	public ExportSegmentAction(SessionEditor editor, long startTime, long endTime, String outputPath) {
		super(editor);
		
		this.segmentType = SegmentType.CUSTOM;
		this.startTime = startTime;
		this.endTime = endTime;
		this.outputPath = outputPath;
	}
	
	private MediaSegment getMediaSegment() {
		if(segmentType == SegmentType.CURRENT_RECORD) {
			Record r = getEditor().currentRecord();
			return (r != null ? r.getSegment().getGroup(0) : null);
		} else if(segmentType == SegmentType.SPEAKER_TURN) {
			return SegmentCalculator.contiguousSegment(getEditor().getSession(), getEditor().getCurrentRecordIndex());
		} else if(segmentType == SegmentType.CONVERSATION_PERIOD) {
			return SegmentCalculator.conversationPeriod(getEditor().getSession(), getEditor().getCurrentRecordIndex());
		} else if(segmentType == SegmentType.CUSTOM) {
			if(startTime < 0 || endTime < 0 || endTime - startTime <= 0) {
				CustomSegmentDialog dlg = new CustomSegmentDialog(getEditor());
				dlg.pack();
				
				dlg.setLocationRelativeTo(getEditor());
				
				dlg.setModal(true);
				dlg.setVisible(true);

				return dlg.getSegment();
			} else {
				MediaSegment retVal = SessionFactory.newFactory().createMediaSegment();
				retVal.setStartValue(startTime);
				retVal.setEndValue(endTime);
				
				return retVal;
			}
		} else {
			return null;
		}
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(outputPath == null) {
			SaveDialogProperties saveProps = new SaveDialogProperties();
			saveProps.setParentWindow(getEditor());
			saveProps.setCanCreateDirectories(true);
			saveProps.setFileFilter(FileFilter.wavFilter);
			saveProps.setMessage("Export audio");
			saveProps.setTitle("Export audio segment");
			saveProps.setRunAsync(true);
			saveProps.setListener(saveListener);
			saveProps.setPrompt("Export");
			
			NativeDialogs.showSaveDialog(saveProps);
		} else {
			doExportTask();
		}
	}
	
	private void doExportTask() {
		ExportTask task = new ExportTask();
		PhonWorker.getInstance().invokeLater(task);
		getEditor().getStatusBar().watchTask(task);
	}
	
	private NativeDialogListener saveListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			if(event.getDialogData() != null) {
				outputPath = event.getDialogData().toString();
				doExportTask();
			}
		}
	};
	
	private class ExportTask extends PhonTask {

		@Override
		public void performTask() {
			setStatus(TaskStatus.RUNNING);
			
			final File outputFile = new File(outputPath);
			
			MediaSegment mediaSegment = getMediaSegment();
			if(mediaSegment != null) {
				SessionMediaModel mediaModel = getEditor().getMediaModel();
				if(!mediaModel.isSessionAudioAvailable()) return;
				
				try {
					LongSound sharedSound = mediaModel.getSharedSessionAudio();
					if(sharedSound == null) return;
					
					ExportSegment exportSegment = sharedSound.getExtension(ExportSegment.class);
					if(exportSegment == null) return;
							
					exportSegment.exportSegment(outputFile, mediaSegment.getStartValue() / 1000.0f, mediaSegment.getEndValue() / 1000.0f);
				} catch (IOException e) {
					LogUtil.severe(e);
					super.err = e;
					setStatus(TaskStatus.ERROR);
					return;
				}
			}
		
			setStatus(TaskStatus.FINISHED);
		}
		
	}
	
}
