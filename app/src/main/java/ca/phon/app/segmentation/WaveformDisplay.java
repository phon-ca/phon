package ca.phon.app.segmentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.plaf.ComponentUI;

import ca.phon.media.sampled.Channel;
import ca.phon.media.sampled.Sampled;

/**
 * Basic waveform display
 *
 */
public class WaveformDisplay extends JComponent implements Scrollable {
	
	private Sampled sampled;
	
	private float startTime = 0.0f;
	
	private float endTime = 0.0f;
	
	/**
	 * Gap between channels in px.
	 * If -1, channels will be stacked on top of each other
	 */
	private int channelGap = 0;
	
	/**
	 * Height (in px) of channels
	 */
	private int channelHeight = 100;
	
	/**
	 * Number of visible channels when inside a {@link JScrollPane}
	 */
	private int visibleChannelCount = 2;
	
	/**
	 * Number of seconds per pixel
	 */
	private float secondsPerPixel = 0.3f;
	
	private List<Channel> availableChannels = new ArrayList<>();
	
	private Map<Channel, Color> channelColors = new HashMap<>();
	
	private Map<Channel, Boolean> channelVisiblity = new HashMap<>();
	
	private final static String uiClassId = "WaveformDisplayUI";
	
	public WaveformDisplay() {
		this(null);
	}
	
	public WaveformDisplay(Sampled sampled) {
		super();
		updateUI();
		
		setSampled(sampled);
	}
	
	@Override
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	protected void setUI(ComponentUI newUI) {
		super.setUI(newUI);
	}

	@Override
	public void updateUI() {
		setUI(new DefaultWaveformDisplayUI());
	}

	public DefaultWaveformDisplayUI getUI() {
		return (DefaultWaveformDisplayUI)ui;
	}

	public Sampled getSampled() {
		return sampled;
	}

	public void setSampled(Sampled sampled) {
		var oldValue = this.sampled;
		this.sampled = sampled;
		
		availableChannels.clear();
		channelVisiblity.clear();
		for(int i = 0; i < sampled.getNumberOfChannels() && i < Channel.values().length; i++) {
			availableChannels.add(Channel.values()[i]);
		}
		
		firePropertyChange("sampled", oldValue, sampled);
	}

	public float getStartTime() {
		return startTime;
	}

	public void setStartTime(float startTime) {
		var oldValue = this.startTime;
		this.startTime = startTime;
		firePropertyChange("startTime", oldValue, startTime);
	}

	public float getEndTime() {
		return endTime;
	}

	public void setEndTime(float endTime) {
		var oldValue = this.endTime;
		this.endTime = endTime;
		firePropertyChange("endTime", oldValue, endTime);
	}
	
	public List<Channel> availableChannels() {
		return Collections.unmodifiableList(this.availableChannels);
	}
	
	public boolean isChannelVisible(Channel ch) {
		return availableChannels.contains(ch) && 
				(channelVisiblity.containsKey(ch) ? channelVisiblity.get(ch) : true);
	}
	
	public void setChannelVisible(Channel ch, boolean visible) {
		var oldValue = isChannelVisible(ch);
		channelVisiblity.put(ch, visible);
		firePropertyChange("channelVisible_" + ch.getName(), oldValue, visible);
	}
	
	public Color getChannelColor(Channel ch) {
		return channelColors.containsKey(ch) ? channelColors.get(ch) : ch.getColor();
	}
	
	public void setChannelColor(Channel ch, Color c) {
		var oldValue = getChannelColor(ch);
		channelColors.put(ch, c);
		firePropertyChange("channelColor_" + ch.getName(), oldValue, c);
	}

	public int getChannelGap() {
		return channelGap;
	}

	public void setChannelGap(int channelGap) {
		var oldValue = this.channelGap;
		this.channelGap = channelGap;
		firePropertyChange("channelGap", oldValue, channelGap);
	}
	
	public int getChannelHeight() {
		return this.channelHeight;
	}
	
	public void setChannelHeight(int channelHeight) {
		var oldValue = this.channelHeight;
		this.channelHeight = channelHeight;
		firePropertyChange("channelHeight", oldValue, channelHeight);
	}
	
	public int getVisibleChannelCount() {
		return visibleChannelCount;
	}

	public void setVisibleChannelCount(int visibleChannelCount) {
		var oldValue = this.visibleChannelCount;
		this.visibleChannelCount = visibleChannelCount;
		firePropertyChange("visibleChannelCount", oldValue, visibleChannelCount);
	}

	public float getSecondsPerPixel() {
		return secondsPerPixel;
	}

	public void setSecondsPerPixel(float secondsPerPixel) {
		this.secondsPerPixel = secondsPerPixel;
	}

	@Override
	public Dimension getPreferredSize() {
		int prefWidth = 
				(int)Math.round(getSampled().getLength() / getSecondsPerPixel());
		
		int prefHeight = (getVisibleChannelCount() * getChannelHeight())
				+ (getChannelGap() * Math.max(getVisibleChannelCount()-1, 0));
		return new Dimension(prefWidth, prefHeight);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		Dimension prefSize = getPreferredSize();
		
		int prefHeight = (getVisibleChannelCount() * getChannelHeight())
				+ (getChannelGap() * Math.max(getVisibleChannelCount()-1, 0));
		return new Dimension(prefSize.width, prefHeight);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

}
