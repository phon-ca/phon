package ca.phon.app.session;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.speech_analysis.actions.ResetAction;
import ca.phon.media.LongSound;
import ca.phon.media.export.VLCWavExporter;
import ca.phon.media.sampled.PCMSampled;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.PhonTaskButton;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.worker.PhonTask.TaskStatus;

/**
 * Media model for a session editor.
 * 
 */
public class SessionMediaModel {
	
	private final Project project;
	
	private final Session session;
	
	private LongSound sharedAudio;
	
	/**
	 * Editor action key generated when session audio file becomes available
	 * The session audio file should be provided as event data
	 */
	public final static String SESSION_AUDIO_AVAILABLE = "session_audio_available";
	
	public SessionMediaModel(Project project, Session session) {
		super();
		
		this.project = project;
		this.session = session;
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	/**
	 * Is session media available?
	 * 
	 * @retrun <code>true</code> if the session media field has been set and
	 *  the media file is available
	 */
	public boolean isSessionMediaAvailable() {
		File sessionMediaFile = getSessionMediaFile();
		return sessionMediaFile != null && sessionMediaFile.exists() && sessionMediaFile.canRead();
	}
	
	/**
	 * Return the media location for the session.
	 * 
	 * @return
	 */
	public File getSessionMediaFile() {
		return MediaLocator.findMediaFile(getProject(), getSession());
	}
	
	/**
	 * Is there an audio file (wav) available for the session?
	 * 
	 * @return <code>true</code> if session media field has been set and a 
	 *  wav file is present for the media
	 */
	public boolean isSessionAudioAvailable() {
		File sessionAudioFile = getSessionAudioFile();
		return sessionAudioFile != null && sessionAudioFile.exists() && sessionAudioFile.canRead();
	}
	
	/**
	 * Return the audio (wav) file for the session. This may be the same
	 * as the session media file.
	 * 
	 * @return session audio (wav) file
	 */
	public File getSessionAudioFile() {
		if(!isSessionMediaAvailable()) return null;
		
		File selectedMedia = getSessionMediaFile();
		File audioFile = null;

		int lastDot = selectedMedia.getName().lastIndexOf('.');
		String mediaName = selectedMedia.getName();
		if(lastDot >= 0) {
			mediaName = mediaName.substring(0, lastDot);
		}
		if(!selectedMedia.isAbsolute()) selectedMedia =
			MediaLocator.findMediaFile(getSession().getMediaLocation(), getProject(), getSession().getCorpus());

		if(selectedMedia != null) {
			File parentFile = selectedMedia.getParentFile();
			audioFile = new File(parentFile, mediaName + ".wav");

			if(!audioFile.exists()) {
				audioFile = null;
			}
		}
		return audioFile;
	}
	
	/**
	 * Return (and load if necessary) the shared session audio 
	 * file instance.
	 * 
	 * @return shared audio file instance
	 * @throws IOException
	 */
	public LongSound getSharedSessionAudio() throws IOException {
		if(this.sharedAudio == null) {
			this.sharedAudio = loadSessionAudio();
		}
		return this.sharedAudio;
	}
	
	/**
	 * Load session audio file as a {@link LongSound} object
	 * 
	 * @return
	 * @throws IOException
	 */
	public LongSound loadSessionAudio() throws IOException {
		if(isSessionAudioAvailable()) {
			File sessionAudio = getSessionAudioFile();
			return LongSound.fromFile(sessionAudio);
		} else {
			throw new FileNotFoundException();
		}
	}
	
}