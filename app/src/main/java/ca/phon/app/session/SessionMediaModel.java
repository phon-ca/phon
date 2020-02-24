package ca.phon.app.session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.GenerateSessionAudioAction;
import ca.phon.media.LongSound;
import ca.phon.media.util.MediaLocator;
import ca.phon.project.Project;
import ca.phon.session.Session;

/**
 * Media model for a session editor.
 * 
 */
public class SessionMediaModel {
	
	private final SessionEditor editor;
	
	private GenerateSessionAudioAction generateSessionAudioAction;
	
	private File loadedAudioFile = null;
	
	private LongSound sharedAudio;
	
	/**
	 * Editor action key generated when session audio file becomes available
	 * The session audio file should be provided as event data
	 */
	public final static String SESSION_AUDIO_AVAILABLE = "session_audio_available";
	
	public SessionMediaModel(SessionEditor editor) {
		super();
		
		this.editor = editor;
	}
	
	public Project getProject() {
		return this.editor.getProject();
	}
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
	public Session getSession() {
		return this.editor.getSession();
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
		if(this.sharedAudio == null || 
				(this.loadedAudioFile != null
					&& isSessionAudioAvailable() && !this.loadedAudioFile.equals(getSessionAudioFile())) ) {
			this.sharedAudio = loadSessionAudio();
		}
		
		if(this.sharedAudio == null) {
			this.sharedAudio = loadSessionAudio();
		} else {
			if(isSessionAudioAvailable()) {
				if(!this.loadedAudioFile.equals(getSessionAudioFile())) {
					this.sharedAudio = loadSessionAudio();
				}
			} else {
				this.sharedAudio = null;
				this.loadedAudioFile = null;
			}
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
			LongSound retVal = LongSound.fromFile(sessionAudio);
			this.loadedAudioFile = sessionAudio;
			return retVal;
		} else {
			throw new FileNotFoundException();
		}
	}
	
	/**
	 * Return this shared action for generating session audio.
	 * Shared access is necessary as multiple views will use
	 * this same action and will want to watch for progress
	 * simultaneously.
	 * 
	 * @return shared generate session audio action
	 */
	public GenerateSessionAudioAction getGenerateSessionAudioAction() {
		if(generateSessionAudioAction == null) {
			generateSessionAudioAction = new GenerateSessionAudioAction(editor);
		}
		return generateSessionAudioAction;
	}
	
}
