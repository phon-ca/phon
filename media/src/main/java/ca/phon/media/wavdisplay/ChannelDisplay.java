/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.media.wavdisplay;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * Display 16-bit waveform audio.
 * 
 *
 */
public class ChannelDisplay extends JComponent {
	
	private static final String uiClassId = "ChannelDisplayUI";
	
	private WavHelper _audioInfo;
	
	private int _channel;
	
	private WavDisplay _parent;
	
	private double _beginTime = 0.0;
	
	private static final int _channelHeight = 40;
	private static final int _defaultWidth = 800;
	
	/**
	 * Constructor.
	 */
	public ChannelDisplay(WavHelper audioInfo, int channel, WavDisplay parent) {
		super();
		
		updateUI();
		
		this._audioInfo = audioInfo;
		this._channel = channel;
		this._parent = parent;
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
		setUI(new DefaultChannelDisplayUI(this));
	}
	
	public ChannelDisplayUI getUI() {
		return (ChannelDisplayUI)ui;
	}

	public WavHelper getAudioInfo() {
		return _audioInfo;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(_defaultWidth, _channelHeight);
	}
	
	public boolean is_loading() {
		return _parent.is_loading();
	}
	
	public int getChannel() {
		return _channel;
	}

	public double get_beginTime() {
		return _beginTime;
	}

	public void set_beginTime(double time) {
		_beginTime = time;
	}

	public WavDisplay get_parent() {
		return _parent;
	}
}
