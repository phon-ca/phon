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
package ca.phon.app.session.editor;

import java.awt.*;
import java.beans.*;
import java.io.*;

import ca.phon.app.log.*;
import ca.phon.app.session.editor.view.media_player.*;
import ca.phon.media.*;
import ca.phon.session.*;
import ca.phon.ui.nativedialogs.*;
import uk.co.caprica.vlcj.player.base.*;

/**
 * Centralized segment playback for the editor with
 * events for playback start/end/progress.
 * 
 * 
 */
public class SegmentPlayback {

	public final static String PLAYBACK_PROP = SegmentPlayback.class.getName() + ".playback";
	
	public final static String TIME_PROP = SegmentPlayback.class.getName() + ".time";
	
	private final SessionEditor editor;
	
	public PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	private volatile float time = 0.0f;
	
	private volatile boolean playing = false;
	
	SegmentPlayback(SessionEditor editor) {
		super();
		
		this.editor = editor;
	}
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
	public boolean isPlaying() {
		return this.playing;
	}
	
	public float getTime() {
		return time;
	}
	
	public void playSegment(float startTime, float endTime) {
		playSegment(Float.valueOf(startTime * 1000.0f).longValue(), Float.valueOf(endTime * 1000.0f).longValue());
	}
	
	public void playSegment(long startTime, long endTime) {
		MediaSegment mediaSeg = SessionFactory.newFactory().createMediaSegment();
		mediaSeg.setStartValue(startTime);
		mediaSeg.setEndValue(endTime);
		playSegment(mediaSeg);
	}
	
	public void playSegment(MediaSegment mediaSegment) {
		if(isPlaying()) return;
		
		EditorViewModel viewModel = getEditor().getViewModel();
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		if(!mediaModel.isSessionMediaAvailable()) {
			getEditor().showMessageDialog("Unable to play segment", "No media available", MessageDialogProperties.okOptions);
			return;
		} else if(!mediaModel.isSessionAudioAvailable() 
				&& !viewModel.isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			getEditor().showMessageDialog("Unable to play segment", "Media player view must be visible", MessageDialogProperties.okOptions);
			return;
		}
		
		if(mediaSegment != null) {
			float startTime = mediaSegment.getStartValue() / 1000.0f;
			float endTime = mediaSegment.getEndValue() / 1000.0f;
			if(mediaModel.isSessionAudioAvailable()) {
				try {
					LongSound longSound = mediaModel.getSharedSessionAudio();
					
					PlaySegment playSeg = longSound.getExtension(PlaySegment.class);
					if(playSeg != null && !playSeg.isPlaying()) {
						playSeg.addPropertyChangeListener(new PlaySegmentPropListener());
						playSeg.playSegment(startTime, endTime, getEditor().getMediaModel().getPlaybackRate());
					}
				} catch (IOException e) {
					LogUtil.warning(e);
				}
			}
			
			MediaPlayerEditorView mediaPlayerView = (MediaPlayerEditorView)viewModel.getView(MediaPlayerEditorView.VIEW_TITLE);
			if(!mediaModel.isSessionAudioAvailable()) {
				mediaPlayerView.getPlayer().addMediaPlayerListener(new MediaPlayerListener(mediaPlayerView));
			}
			mediaPlayerView.getPlayer().playSegment(Float.valueOf(mediaSegment.getStartValue()).longValue(), 
					Float.valueOf(mediaSegment.getEndValue()-mediaSegment.getStartValue()).longValue(), mediaModel.isSessionAudioAvailable());
		}
	}
	
	public void stopPlaying() {
		if(!isPlaying()) return;
		
		if(getEditor().getMediaModel().isSessionAudioAvailable()) {
			try {
				LongSound ls = getEditor().getMediaModel().getSharedSessionAudio();
				
				PlaySegment playSeg = ls.getExtension(PlaySegment.class);
				if(playSeg != null && playSeg.isPlaying())
					playSeg.stop();
			} catch (IOException e) {
				LogUtil.severe(e);
				Toolkit.getDefaultToolkit().beep();
			}
		}
		if(getEditor().getMediaModel().isSessionMediaAvailable()
				&& getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			MediaPlayerEditorView mediaPlayerView = (MediaPlayerEditorView)getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaPlayerView.getPlayer().isPlaying())
				mediaPlayerView.getPlayer().pause();
		}
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

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(PropertyChangeEvent event) {
		propSupport.firePropertyChange(event);
	}

	public boolean hasListeners(String propertyName) {
		return propSupport.hasListeners(propertyName);
	}
	
	private class MediaPlayerListener extends MediaPlayerEventAdapter {
		
		MediaPlayerEditorView mediaPlayerView;
		
		public MediaPlayerListener(MediaPlayerEditorView view) {
			this.mediaPlayerView = view;
		}
		
		@Override
		public void playing(MediaPlayer mediaPlayer) {
			playing = true;
			firePropertyChange(PLAYBACK_PROP, false, true);
		}

		@Override
		public void paused(MediaPlayer mediaPlayer) {
			playing = false;
			firePropertyChange(PLAYBACK_PROP, true, false);
			mediaPlayerView.getPlayer().removeMediaPlayerListener(this);
		}

		@Override
		public void timeChanged(MediaPlayer mediaPlayer, long nt) {
			float oldTime = time;
			float newTime = (nt / 1000.0f);
			firePropertyChange(TIME_PROP, oldTime, newTime);
		}
		
	}
	
	private class PlaySegmentPropListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			PlaySegment playSeg = (PlaySegment)evt.getSource();
			if("playing".equals(evt.getPropertyName())) {
				playing = (boolean)evt.getNewValue();
				firePropertyChange(PLAYBACK_PROP, evt.getOldValue(), evt.getNewValue());
				
				if(!playing) {
					playSeg.removePropertyChangeListener(this);
				}
			} else if("position".equals(evt.getPropertyName())) {
				time = (float)evt.getNewValue();
				firePropertyChange(TIME_PROP, evt.getOldValue(), evt.getNewValue());
			}
		}
		
	};
	
}
