package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import ca.phon.app.log.BufferWindow;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.audio.AudioFileInfo;
import ca.phon.audio.AudioIO;
import ca.phon.audio.InvalidHeaderException;
import ca.phon.audio.UnsupportedFormatException;
import ca.phon.util.OSInfo;

public class ShowMediaInfoAction extends SessionEditorAction {
	
	private final static String TXT = "Show media information";
	
	private final static String DESC = "Show media information in new buffer window";

	public ShowMediaInfoAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(!mediaModel.isSessionMediaAvailable()) return;
		
		File mediaFile = mediaModel.getSessionMediaFile();
		File audioFile = mediaModel.getSessionAudioFile();
		
		StringBuffer buf = new StringBuffer();
		if(audioFile == null) {
			// no audio
			appendMediaInformation(mediaFile, buf);
		} else {
			if(!audioFile.equals(mediaFile)) {
				appendMediaInformation(mediaFile, buf);
			}
			appendAudioInformation(audioFile, buf);
		}
		
		BufferWindow window = BufferWindow.getBufferWindow();
		window.createBuffer("Media Information", true).getLogBuffer().append(buf.toString());
		window.showWindow();
	}
	
	private void appendMediaInformation(File mediaFile, StringBuffer buffer) {
		
	}
	
	private void appendAudioInformation(File audioFile, StringBuffer buffer) {
		try {
			final AudioFileInfo audioInfo = AudioIO.checkHeaders(audioFile);
			
			String nl = OSInfo.isWindows() ? "\r\n" : "\n";
			buffer.append("Session media:\t").append(audioFile).append(nl);
			buffer.append("File type:\t").append(audioInfo.getFileType()).append(nl);
			buffer.append("Encoding:\t").append(audioInfo.getEncoding()).append(nl);
			buffer.append("Channels:\t").append(audioInfo.getNumberOfChannels()).append(nl);
			buffer.append("Sample rate:\t").append(audioInfo.getSampleRate()).append(nl);
			buffer.append("Number of samples:\t").append(audioInfo.getNumberOfSamples()).append(nl);
			
		} catch (InvalidHeaderException | UnsupportedFormatException | IOException e) {
			LogUtil.severe(e);
		}
	}

}
