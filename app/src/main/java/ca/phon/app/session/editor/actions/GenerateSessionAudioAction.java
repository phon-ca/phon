package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import ca.phon.app.session.SessionMediaModel;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.speech_analysis.actions.ResetAction;
import ca.phon.media.export.VLCWavExporter;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.worker.PhonTask.TaskStatus;

/**
 * Generate session audio file
 * 
 */
public class GenerateSessionAudioAction extends SessionEditorAction {

	private final static String TXT = "Generate session audio";
	
	private final static String DESC = "Generate session audio file from media";
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("misc/oscilloscope", IconSize.SMALL);
	
	private final Collection<PhonTaskListener> customListeners = new ArrayList<>();
	
	public GenerateSessionAudioAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}
	
	public void addTaskListener(PhonTaskListener listener) {
		customListeners.add(listener);
	}
	
	public void removeTaskListener(PhonTaskListener listener) {
		customListeners.remove(listener);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		PhonTask exportTask = generateExportAudioTask();
		getEditor().getStatusBar().watchTask(exportTask);
		
		PhonWorker worker = PhonWorker.createWorker();
		worker.setName("Generate session audio");
		worker.setFinishWhenQueueEmpty(true);
		worker.invokeLater(exportTask);
		worker.start();
	}
	
	/**
	 * Provide access to the internal task for generating
	 * session audio file.
	 * 
	 */
	public PhonTask generateExportAudioTask() {
		PhonTaskListener taskListener = new PhonTaskListener() {

			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus,
					TaskStatus newStatus) {
				if(newStatus == TaskStatus.FINISHED) {
					if(getEditor().getMediaModel().isSessionAudioAvailable()) {
						// tell the editor session audio is now available
						EditorEvent ee = new EditorEvent(SessionMediaModel.SESSION_AUDIO_AVAILABLE, GenerateSessionAudioAction.this, 
								getEditor().getMediaModel().getSessionAudioFile());
						getEditor().getEventManager().queueEvent(ee);
					}
				}
			}

			@Override
			public void propertyChanged(PhonTask task, String property,
					Object oldValue, Object newValue) {
			}

		};
		
		PhonTask exportTask = generateAudioFileTask();
		exportTask.addTaskListener(taskListener);
		customListeners.forEach(exportTask::addTaskListener);
		
		return exportTask;
	}
	
	/**
	 * Create, if possible, a new task for generating session audio file.
	 *  
	 * @return the new task or <code>null</code> if task creation failed.
	 */
	private PhonTask generateAudioFileTask() {
		final Session session = getEditor().getSession();
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		
		if(mediaModel.isSessionMediaAvailable()) {
			File movFile = MediaLocator.findMediaFile(
				session.getMediaLocation(), getEditor().getProject(), session.getCorpus());
			int lastDot = movFile.getName().lastIndexOf(".");
			if(lastDot > 0) {
				String movExt = movFile.getName().substring(lastDot);
				if(movExt.equals(".wav")) {
					// already a wav, do nothing!
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(CommonModuleFrame.getCurrentFrame());
					props.setTitle("Generate Wav");
					props.setHeader("Failed to generate wav");
					props.setMessage("Source file is already in wav format.");
					props.setRunAsync(false);
					props.setOptions(MessageDialogProperties.okOptions);
					NativeDialogs.showMessageDialog(props);
					return null;
				}
				String audioFileName =
					movFile.getName().substring(0, movFile.getName().lastIndexOf(".")) +
						".wav";
				File parentFile = movFile.getParentFile();
				File resFile = new File(parentFile, audioFileName);

				if(resFile.exists()) {
					// ask to overwrite
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(CommonModuleFrame.getCurrentFrame());
					props.setTitle("Generate Wav");
					props.setHeader("Overwrite file?");
					props.setMessage("Wav file already exists, overwrite?");
					props.setRunAsync(false);
					props.setOptions(MessageDialogProperties.yesNoOptions);
					int retVal = NativeDialogs.showMessageDialog(props);
					if(retVal != 0) return null;
				}

				final VLCWavExporter exporter = new VLCWavExporter(movFile, resFile);
				return exporter;
			}
		}
		return null;
	}

}
