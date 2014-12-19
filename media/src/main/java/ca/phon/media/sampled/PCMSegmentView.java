package ca.phon.media.sampled;

import java.awt.Color;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.Mixer.Info;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import ca.phon.media.exceptions.PhonMediaException;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
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
		return (getSegmentStart() > 0.0f && getSegmentLength() > 0.0f);
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
		return (getSelectionStart() > 0.0f && getSelectionLength() > 0.0f);
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
	
	public AudioFormat getAudioFormat() {
		final AudioFormat format = new AudioFormat(getSampled().getSampleRate(), 
				getSampled().getSampleSize(), getSampled().getNumberOfChannels(), 
				getSampled().isSigned(), false);
		return format;
	}
	
	private void playSection(float startTime, float length) {
		final AudioFormat format = getAudioFormat();
		final byte[] audioData = getSampled().getBytes(startTime, startTime+length);
		// playback audio using Clip
		try {
			final Clip audioClip = AudioSystem.getClip(getMixerInfo());
			audioClip.open(format, audioData, 0, audioData.length);
			audioClip.start();
		} catch (LineUnavailableException e) {
		}
	}
	
	public void saveToFile(File file, float startTime, float length) 
		throws IOException {
		final byte[] bytes = getSampled().getBytes(startTime, startTime + length);
		final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		final long len = bytes.length;
		final AudioFormat format = getAudioFormat();
		
		final AudioInputStream aio = new AudioInputStream(bin, format, len);
		AudioSystem.write(aio, Type.WAVE, file);
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
