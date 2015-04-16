/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.media.sampled;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.plaf.ComponentUI;

import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;

/**
 * Display a window of PCM data.
 */
public class PCMSegmentView extends JComponent {
	
	private final static Logger LOGGER = Logger.getLogger(PCMSegmentView.class.getName());
	
	private static final long serialVersionUID = -1882617273398866548L;
	
	/**
	 * Default height for channels
	 */
	public final static int DEFAULT_CHANNEL_HEIGHT = 100;
	
	/**
	 * Default samples per pixel
	 */
	public final static int DEFAULT_SAMPLES_PER_PIXEL = 320;

	/**
	 * Sampled object
	 */
	private Sampled sampled;
	public static final String SAMPLED_PROP = "sampled";
	
	/**
	 * Window start (seconds)
	 */
	private float windowStart;
	public static final String WINDOW_START_PROT = "windowStart";
	
	/**
	 * Window length
	 */
	private float windowLength;
	public static final String WINDOW_LENGTH_PROP = "windowLength";
	
	/**
	 * Segment start
	 */
	private float segmentStart;
	public static final String SEGMENT_START_PROP = "segmentStart";
	
	/**
	 * Segment length
	 */
	private float segmentLength;
	public static final String SEGMENT_LENGTH_PROP = "segmentLength";
	
	/**
	 * Selection start
	 */
	private float selectionStart;
	public static final String SELECTION_START_PROP = "selectionStart";
	
	/**
	 * Selection length
	 */
	private float selectionLength;
	public static final String SELECTION_LENGTH_PROP = "selectionLength";
	
	/**
	 * Color for non-segment portion (left/right border)
	 */
	private Color excludedColor = new Color(200, 200, 200, 100);
	public static final String EXCLUDED_COLOR_PROP = "excludedColor";
	
	/**
	 * Color for selection
	 */
	private Color selectionColor = new Color(50, 125, 200, 100);
	public static final String SELECTION_COLOR_PROP = "selectionColor";
	
	/**
	 * Are we playing audio?
	 */
	private boolean playing = false;
	public static final String PLAYING_PROP = "playing";
	
	/**
	 * Loop playback
	 */
	private boolean loop = false;
	public static final String LOOP_PROP = "loop";
	
	/**
	 * Location (in seconds) of the playback marker
	 */
	private float playbackMarker = 0.0f;
	public static final String PLAYBACK_MARKER_PROP = "playbackMarker";
	
	/**
	 * Playback marker task
	 */
	private AtomicReference<PlaybackMarkerTask> playbackTaskRef = 
			new AtomicReference<PCMSegmentView.PlaybackMarkerTask>();

	/**
	 * Channel color map
	 */
	private Map<Channel, Color> channelColors = Channel.createColorMap();
	
	/**
	 * Indexed property for channel color
	 */
	public static final String CHANNEL_COLOR_PROP = "channelColor";
	
	/**
	 * Channel visibility, by default all channels are visible
	 */
	public Map<Channel, Boolean> channelVisiblity = new HashMap<Channel, Boolean>();
	
	/**
	 * Indexed property for channel visiblity
	 */
	public static final String CHANNEL_VISIBLITY_PROP = "channelVisibility";
	
	
	/**
	 * Cursor location
	 */
	private int cursorLocation = -1;
	public static final String CURSOR_LOCATION_PROP = "cursorLocation";
	
	/**
	 * Selected mixer, null = default
	 */
	private Info mixerInfo = null;
	
	/**
	 * Flag used to control repainting when adjusting values
	 */
	private boolean valuesAreAdjusting = false;
	
	public PCMSegmentView() {
		super();
		this.sampled = null;
		this.windowStart = 0.0f;
		this.windowLength = 0.0f;
		
		updateUI();
	}
	
	public PCMSegmentView(Sampled sampled) {
		super();
		this.sampled = sampled;
		this.windowStart = sampled.getStartTime();
		this.windowLength = sampled.getLength();

		updateUI();
	}
	
	public PCMSegmentView(Sampled sampled, float windowStart, float windowLength) {
		super();
		this.sampled = sampled;
		this.windowStart = windowStart;
		this.windowLength = windowLength;
		
		updateUI();
	}
	
	public Sampled getSampled() {
		return sampled;
	}

	public void setSampled(Sampled sampled) {
		Sampled oldVal = getSampled();
		this.sampled = sampled;
		super.firePropertyChange(SAMPLED_PROP, oldVal, sampled);
	}

	public float getWindowStart() {
		return windowStart;
	}

	public void setWindowStart(float windowStart) {
		float oldVal = getWindowStart();
		this.windowStart = windowStart;
		super.firePropertyChange(WINDOW_START_PROT, oldVal, windowStart);
	}

	public float getWindowLength() {
		return windowLength;
	}

	public void setWindowLength(float windowLength) {
		float oldVal = getWindowLength();
		this.windowLength = windowLength;
		super.firePropertyChange(WINDOW_LENGTH_PROP, oldVal, windowLength);
	}

	public float getSegmentStart() {
		return segmentStart;
	}

	public void setSegmentStart(float segmentStart) {
		float oldVal = getSegmentStart();
		this.segmentStart = segmentStart;
		super.firePropertyChange(SEGMENT_START_PROP, oldVal, segmentStart);
	}

	public float getSegmentLength() {
		return segmentLength;
	}

	public void setSegmentLength(float segmentLength) {
		float oldVal = getSegmentLength();
		this.segmentLength = segmentLength;
		super.firePropertyChange(SEGMENT_LENGTH_PROP, oldVal, segmentLength);
	}
	
	public boolean hasSegment() {
		return (getSegmentStart() >= 0.0f && getSegmentLength() > 0.0f);
	}

	public float getSelectionStart() {
		return selectionStart;
	}

	public void setSelectionStart(float selectionStart) {
		float oldVal = getSelectionStart();
		this.selectionStart = selectionStart;
		super.firePropertyChange(SELECTION_START_PROP, oldVal, selectionStart);
	}
	
	public float getSelectionLength() {
		return selectionLength;
	}

	public void setSelectionLength(float selectionLength) {
		float oldVal =  getSelectionLength();
		this.selectionLength = selectionLength;
		super.firePropertyChange(SELECTION_LENGTH_PROP, oldVal, selectionLength);
	}

	public boolean hasSelection() {
		return (getSelectionStart() >= 0.0f && getSelectionLength() > 0.0f);
	}
	
	public Color getExcludedColor() {
		return excludedColor;
	}

	public void setExcludedColor(Color excludedColor) {
		Color oldVal = getExcludedColor();
		this.excludedColor = excludedColor;
		super.firePropertyChange(EXCLUDED_COLOR_PROP, oldVal, excludedColor);
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor) {
		Color oldVal = getSelectionColor();
		this.selectionColor = selectionColor;
		super.firePropertyChange(SELECTION_COLOR_PROP, oldVal, selectionColor);
	}
	
	public Color getChannelColor(Channel channel) {
		return channelColors.get(channel);
	}
	
	public void setChannelColor(Channel channel, Color color) {
		Color oldVal = getChannelColor(channel);
		channelColors.put(channel, color);
		super.firePropertyChange(CHANNEL_COLOR_PROP, oldVal, color);
	}
	
	public boolean isChannelVisible(Channel channel) {
		return (channelVisiblity.get(channel) != null ? channelVisiblity.get(channel) : true);
	}
	
	public void setChannelVisible(Channel channel, boolean visible) {
		boolean wasVisible = isChannelVisible(channel);
		channelVisiblity.put(channel, visible);
		super.firePropertyChange(CHANNEL_VISIBLITY_PROP, wasVisible, visible);
	}
	
	public int getCursorPosition() {
		return this.cursorLocation;
	}
	
	public void setCursorPosition(int position) {
		int oldVal = getCursorPosition();
		this.cursorLocation = position;
		super.firePropertyChange(CURSOR_LOCATION_PROP, oldVal, position);
	}
	
	public boolean isPlaying() {
		return this.playing;
	}
	
	public void setPlaying(boolean playing) {
		boolean oldVal = this.playing;
		this.playing = playing;
		super.firePropertyChange(PLAYING_PROP, oldVal, playing);
	}
	
	private PlaybackMarkerTask getPlaybackTask() {
		return playbackTaskRef.get();
	}
	
	private void setPlaybackTask(PlaybackMarkerTask playbackTask) {
		playbackTaskRef.set(playbackTask);
	}
	
	public boolean isValuesAdjusting() {
		return this.valuesAreAdjusting;
	}
	
	public void setValuesAdusting(boolean valuesAdjusting) {
		this.valuesAreAdjusting = valuesAdjusting;
	}
	
	public Info getMixerInfo() {
		if(this.mixerInfo == null) {
			return AudioSystem.getMixerInfo()[0];
		} else 
			return this.mixerInfo;
	}
	
	public void setMixerInfo(Info info) {
		this.mixerInfo = info;
	}
	
	public float getPlaybackMarker() {
		return this.playbackMarker;
	}
	
	public void setPlaybackMarker(float playbackMarker) {
		float oldVal = this.playbackMarker;
		this.playbackMarker = playbackMarker;
		super.firePropertyChange(PLAYBACK_MARKER_PROP, oldVal, playbackMarker);
	}
	
	/**
	 * Convert a model value in time to a horizontal position
	 * in pixels.
	 * 
	 * @param time (in seconds)
	 * 
	 * @return x position
	 */
	public double modelToView(float time) {
		final float msPerPixel = getWindowLength() / getWidth();
		return (time - getWindowStart()) / msPerPixel;
	}
	
	public float viewToModel(double x) {
		final float msPerPixel = getWindowLength() / getWidth();
		return getWindowStart() + (float)(x * msPerPixel);
	}
	
	/**
	 * Play the current segment
	 */
	public void playSegment() {
		if(hasSegment())
			playSection(getSegmentStart(), getSegmentLength());
	}
	
	/**
	 * Play the current selection
	 */
	public void playSelection() {
		if(hasSelection())
			playSection(getSelectionStart(), getSelectionLength());
	}
	
	/**
	 * Play the current selection (if any) or segment.
	 * 
	 */
	public void play() {
		if(hasSelection()) {
			playSelection();
		} else {
			playSegment();
		}
	}
	
	public void stop() {
		final PlaybackMarkerTask task = getPlaybackTask();
		
		if(task != null && task.clip != null && task.clip.isActive()) {
			task.clip.stop();
		}
	}
	
	public boolean isLoop() {
		return this.loop;
	}
	
	public void setLoop(boolean loop) {
		boolean oldVal = this.loop;
		this.loop = loop;
		super.firePropertyChange(LOOP_PROP, oldVal, loop);
	}
	
	public AudioFormat getAudioFormat() {
		final AudioFormat format = new AudioFormat(getSampled().getSampleRate(), 
				getSampled().getSampleSize(), getSampled().getNumberOfChannels(), 
				getSampled().isSigned(), false);
		return format;
	}
	
	private void playSection(final float startTime, float length) {
		if(isPlaying()) return;
		
		final AudioFormat format = getAudioFormat();
		final byte[] audioData = getSampled().getBytes(startTime, startTime+length);
		// playback audio using Clip
		try {
			final Clip audioClip = AudioSystem.getClip(getMixerInfo());
			audioClip.open(format, audioData, 0, audioData.length);
			final LineListener lineListener = new LineListener() {
				
				@Override
				public void update(LineEvent event) {
					if(event.getType() == LineEvent.Type.START) {
						setPlaying(true);
						final PlaybackMarkerTask task = new PlaybackMarkerTask(audioClip, startTime);
						setPlaybackTask(task);
						task.execute();
					} else if(event.getType() == LineEvent.Type.STOP) {
						setPlaying(false);
						setPlaybackTask(null);
						event.getLine().close();
					}
				}
				
			};
			audioClip.addLineListener(lineListener);
			
			if(isLoop())
				audioClip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				audioClip.start();
			
		} catch (LineUnavailableException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private class PlaybackMarkerTask extends SwingWorker<Float, Float> {
		
		private final Clip clip;
		
		private final float startTime;
		
		public PlaybackMarkerTask(Clip clip, float startTime) {
			this.clip = clip;
			this.startTime = startTime;
		}

		@Override
		protected Float doInBackground() throws Exception {
			while(isPlaying() && clip.isOpen()) {
				final long clipPos = clip.getMicrosecondPosition() % clip.getMicrosecondLength();
				final float lineMs = clipPos / 1000.0f / 1000.0f;
				
				final float currentTime = startTime + lineMs;
				publish(currentTime);
				
				try { Thread.sleep(10); } catch(Exception e) {}
			}
			return 0.0f;
		}

		@Override
		protected void process(List<Float> chunks) {
			// only use the last value
			setPlaybackMarker(chunks.get(chunks.size()-1));
		}
		
	}
	
	public void saveToFile(File file, float startTime, float length) 
		throws IOException {
		final byte[] bytes = getSampled().getBytes(startTime, startTime + length);
		final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		final long len = bytes.length;
		final AudioFormat format = getAudioFormat();
		
		final AudioInputStream aio = new AudioInputStream(bin, format, (len/format.getFrameSize()));
		AudioSystem.write(aio, Type.WAVE, file);
	}
	
	public void save() {
		if(hasSelection()) {
			saveSelection();
		} else if(hasSegment()) {
			saveSegment();
		}
	}
	
	public void saveSegment() {
		if(hasSegment()) {
			saveSection(getSegmentStart(), getSegmentLength());
		}
	}
	
	public void saveSelection() {
		if(hasSelection()) {
			saveSection(getSelectionStart(), getSelectionLength());
		}
	}
	
	private void saveSection(float startTime, float length) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.wavFilter);
		props.setRunAsync(true);
		props.setListener(new SaveDialogListener(startTime, length));
		
		NativeDialogs.showSaveDialog(props);
	}
	
	private class SaveDialogListener implements NativeDialogListener {
		
		private float startTime = 0.0f;
		
		private float length = 0.0f;
		
		public SaveDialogListener(float startTime, float len) {
			this.startTime = startTime;
			this.length = len;
		}
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent evt) {
			if(evt.getDialogResult() == NativeDialogEvent.OK_OPTION) {
				final String filename = 
						(evt.getDialogData() != null ? evt.getDialogData().toString() : null);
				if(filename != null) {
					try {
						saveToFile(new File(filename), startTime, length);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			}
		}

	}
	
	@Override
	public String getUIClassID() {
		return PCMSegmentViewUI.class.getName();
	}

	@Override
	public void updateUI() {
		setUI(new DefaultPCMSegmentViewUI(this));
	}
	
	@Override
	public void setUI(ComponentUI newUI) {
		super.setUI(newUI);
	}
	
}
