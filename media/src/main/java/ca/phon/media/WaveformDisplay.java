/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.media;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Basic waveform display
 *
 */
public class WaveformDisplay extends TimeComponent implements Scrollable {
	
	private LongSound longSound;
		
	/**
	 * Height (in px) of channels
	 */
	private int preferredChannelHeight = 100;
	
	private List<Channel> availableChannels = new ArrayList<>();
	
	private Map<Channel, Boolean> channelVisiblity = new HashMap<>();
	
	private boolean trackViewportWidth = false;
	
	private boolean trackViewportHeight = false;
	
	private final static String uiClassId = "WaveformDisplayUI";
	
	private Insets channelInsets = new Insets(5, 0, 5, 0);
	
	public WaveformDisplay() {
		this(new TimeUIModel());
	}
	
	public WaveformDisplay(TimeUIModel timeModel) {
		this(null, timeModel);
	}
	
	public WaveformDisplay(LongSound longSound, TimeUIModel timeModel) {
		super(timeModel);
		setLongSound(longSound);
		
		updateUI();
	}
	
	@Override
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	public void updateUI() {
		setUI(new DefaultWaveformDisplayUI());
	}

	public DefaultWaveformDisplayUI getUI() {
		return (DefaultWaveformDisplayUI)ui;
	}

	public LongSound getLongSound() {
		return this.longSound;
	}

	public void setLongSound(LongSound longSound) {
		var oldValue = this.longSound;
		this.longSound = longSound;
		
		availableChannels.clear();
		channelVisiblity.clear();
		if(longSound != null) {
			for(int i = 0; i < longSound.numberOfChannels() && i < Channel.values().length; i++) {
				availableChannels.add(Channel.values()[i]);
				channelVisiblity.put(Channel.values()[i], true);
			}
		}
		
		firePropertyChange("longSound", oldValue, longSound);
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

	public int getChannelHeight() {
		int retVal = getPreferredChannelHeight();
		
		if(isTrackViewportHeight() && getHeight() > 0) {
			int visibleChannels = getVisibleChannelCount();
			int height = getHeight() - (visibleChannels * (getChannelInsets().top+getChannelInsets().bottom));
			if(visibleChannels > 0) {
				retVal = height / visibleChannels;
			}
		}
		
		return retVal;
	}
	
	public int getPreferredChannelHeight() {
		return this.preferredChannelHeight;
	}
	
	public void setPreferredChannelHeight(int channelHeight) {
		var oldValue = this.preferredChannelHeight;
		this.preferredChannelHeight = channelHeight;
		firePropertyChange("preferredChannelHeight", oldValue, channelHeight);
	}
	
	public int getVisibleChannelCount() {
		int visibleChannels = 0;
		for(Channel ch:availableChannels()) {
			if(isChannelVisible(ch))
				visibleChannels++;
		}
		return visibleChannels;
	}
		
	public boolean isTrackViewportWidth() {
		return this.trackViewportWidth;
	}
	
	public void setTrackViewportWidth(boolean trackViewportWidth) {
		var oldVal = this.trackViewportWidth;
		this.trackViewportWidth = trackViewportWidth;
		super.firePropertyChange("trackViewportWidth", oldVal, trackViewportWidth);
	}
	
	public boolean isTrackViewportHeight() {
		return this.trackViewportHeight;
	}
	
	public void setTrackViewportHeight(boolean trackViewportHeight) {
		var oldVal = this.trackViewportHeight;
		this.trackViewportHeight = trackViewportHeight;
		super.firePropertyChange("trackViewportHeight", oldVal, trackViewportHeight);
	}
	
	public Insets getChannelInsets() {
		return this.channelInsets;
	}
	
	public void setChannelInsets(Insets channelInsets) {
		var oldVal = this.channelInsets;
		this.channelInsets = channelInsets;
		super.firePropertyChange("channelInsets", oldVal, channelInsets);
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		Dimension prefSize = getPreferredSize();
		
		int prefHeight = (getVisibleChannelCount() * getChannelHeight())
				+ (getVisibleChannelCount() * (getChannelInsets().top + getChannelInsets().bottom));
		
		if(prefHeight == 0) {
			prefHeight = getChannelHeight() + (getChannelInsets().top + getChannelInsets().bottom);
		}
		
		return new Dimension(prefSize.width, prefHeight);
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 100;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return isTrackViewportWidth();
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return isTrackViewportHeight();
	}

}
