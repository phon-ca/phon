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

import ca.phon.extensions.Extension;

import java.beans.*;
import java.io.IOException;

/**
 * Extension interface for LongSound objects.
 * 
 * <p>E.g.,
 * <pre>
 * float startTime, endTime = ... // setup segment times
 * PlaySegment playSeg = longSound.getExtension(PlaySegment.class);
 * if(playSeg != null) {
 *     playSeg.playSegment(startTime, endTime);
 * }
 * </pre>
 * </p>
 * 
 */
@Extension(LongSound.class)
public abstract class PlaySegment {
	
	private volatile boolean playing = false;
	
	private volatile boolean loop = false;
	
	private volatile float position = -1.0f;
	
	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	private final VolumeModel volumeModel;
	
	public PlaySegment() {
		this(new VolumeModel());
	}

	public PlaySegment(VolumeModel volumeModel) {
		super();
		this.volumeModel = volumeModel;
	}
	
	/**
	 * Play segment
	 * 
	 * @param startTime
	 * @param endTime
	 * 
	 * @throws IOException on error
	 */
	public abstract void playSegment(float startTime, float endTime, float playbackRate) throws IOException;
	
	/**
	 * Stop playing segment.
	 * 
	 */
	public abstract void stop();

	public boolean isLoop() {
		return this.loop;
	}
	
	public void setLoop(boolean loop) {
		var oldVal = this.loop;
		this.loop = loop;
		propSupport.firePropertyChange("loop", oldVal, loop);
	}
	
	public boolean isPlaying() {
		return this.playing;
	}
	
	public void setPlaying(boolean playing) {
		var oldVal = this.playing;
		this.playing = playing;
		propSupport.firePropertyChange("playing", oldVal, playing);
	}
	
	public float getPosition() {
		return this.position;
	}
	
	public void setPosition(float position) {
		var oldVal = this.position;
		this.position = position;
		propSupport.firePropertyChange("position", oldVal, position);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propSupport.getPropertyChangeListeners();
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return propSupport.getPropertyChangeListeners(propertyName);
	}
	
}
