package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import ca.phon.app.log.BufferWindow;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.audio.AudioFileInfo;
import ca.phon.audio.AudioIO;
import ca.phon.audio.InvalidHeaderException;
import ca.phon.audio.UnsupportedFormatException;
import ca.phon.util.MsFormatter;
import ca.phon.util.OSInfo;
import uk.co.caprica.vlcj.media.AudioTrackInfo;
import uk.co.caprica.vlcj.media.InfoApi;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.VideoTrackInfo;

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
		if(!mediaModel.isSessionMediaAvailable()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		
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
		MediaPlayerEditorView mediaPlayerView = (MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaPlayerView == null) return;
		
		InfoApi mediaInfo = mediaPlayerView.getPlayer().getMediaPlayer().media().info();

		String nl = OSInfo.isWindows() ? "\r\n" : "\n";
		buffer.append("Session media:\t\t").append(mediaFile).append(nl);
		buffer.append("Length:\t\t\t").append(MsFormatter.msToDisplayString(mediaInfo.duration())).append(nl);
		buffer.append("Track info:").append(nl);
		
		List<VideoTrackInfo> videoTracks = mediaInfo.videoTracks();
		for(int i = 0; i < videoTracks.size(); i++) {
			VideoTrackInfo vti = videoTracks.get(i);
			buffer.append("\t").append("Video track:\t").append((i+1)).append(" of ").append(videoTracks.size()).append(nl);
			buffer.append("\t").append("Size:\t\t").append(vti.width()).append("x").append(vti.height()).append(nl);
			buffer.append("\t").append("Codec:\t\t").append(vti.codecName()).append(" (").append(vti.codecDescription()).append(")").append(nl);
			buffer.append("\t").append("Frame rate:\t").append(vti.frameRate() / (float)vti.frameRateBase()).append(nl);
			buffer.append(nl);
		}
		
		List<AudioTrackInfo> audioTracks = mediaInfo.audioTracks();
		for(int i = 0; i < audioTracks.size(); i++) {
			AudioTrackInfo ati = audioTracks.get(i);
			buffer.append("\t").append("Audio track:\t").append((i+1)).append(" of ").append(audioTracks.size()).append(nl);
			buffer.append("\t").append("Channels:\t\t").append(ati.channels()).append(nl);
			buffer.append("\t").append("Codec:\t\t").append(ati.codecName()).append(" (").append(ati.codecDescription()).append(")").append(nl);
			buffer.append("\t").append("Sample rate:\t").append(ati.rate()).append(nl);
			buffer.append(nl);
		}
		
		buffer.append(nl);
	}
	
	private void appendAudioInformation(File audioFile, StringBuffer buffer) {
		try {
			final AudioFileInfo audioInfo = AudioIO.checkHeaders(audioFile);
			
			String nl = OSInfo.isWindows() ? "\r\n" : "\n";
			buffer.append("Session audio:\t\t").append(audioFile).append(nl);
			buffer.append("File type:\t\t").append(audioInfo.getFileType()).append(nl);
			buffer.append("Encoding:\t\t\t").append(audioInfo.getEncoding()).append(nl);
			buffer.append("Channels:\t\t\t").append(audioInfo.getNumberOfChannels()).append(nl);
			buffer.append("Sample rate:\t\t").append(audioInfo.getSampleRate()).append(nl);
			buffer.append("Number of samples:\t").append(audioInfo.getNumberOfSamples()).append(nl);
			buffer.append("Length:\t\t\t").append(MsFormatter.msToDisplayString((long)(audioInfo.getNumberOfSamples() / audioInfo.getSampleRate() * 1000.0))).append(nl);
			buffer.append(nl);
		} catch (InvalidHeaderException | UnsupportedFormatException | IOException e) {
			LogUtil.severe(e);
		}
	}

}
