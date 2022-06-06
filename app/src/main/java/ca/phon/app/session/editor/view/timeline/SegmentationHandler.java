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
package ca.phon.app.session.editor.view.timeline;

import java.awt.*;
import java.awt.event.*;
import java.security.Key;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.undo.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.media_player.*;
import ca.phon.app.session.editor.view.media_player.actions.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.media.*;
import ca.phon.media.player.*;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.*;
import ca.phon.ui.nativedialogs.*;
import uk.co.caprica.vlcj.player.base.*;

public final class SegmentationHandler {

	/* Editor events */
	public final static String EDITOR_SEGMENTATION_START = "_segmentation_start_";
	
	public final static String EDITOR_SEGMENTATION_END = "_segmentation_end_";
	
	private final static int VOLUME_INCR = 5;
	
	private final static long MIN_SEGMENT_LENGTH = 50L;
	
	private final static long MIN_SEGMENT_WINDOW = 100L;
	
	private final static long MAX_SEGMENT_WINDOW = 10000L;
	
	private final static long SEGMENT_WINDOW_DELTA = 100L;
	
	public static enum SegmentationMode {
		INSERT_AT_END("Insert record at end of session"),
		INSERT_AFTER_CURRENT("Insert record after current one"),
		REPLACE_CURRENT("Replace segment for current record");
		
		String val = "";
		
		SegmentationMode(String txt) {
			val = txt;
		}
		
		@Override
		public String toString() {
			return this.val;
		}
		
		public static SegmentationMode fromString(String value) {
			for(SegmentationMode v:values()) {
				if(v.toString().equals(value)) {
					return v;
				}
			}
			return null;
		}
		
	}
	
	public static enum MediaStart {
		AT_BEGNINNING("Play media from beginning"),
		FROM_CURRENT_POSITION("Play media from current position"),
		AT_END_OF_LAST_RECORD("Play media from end of last record"),
		AT_END_OF_LAST_RECORD_FOR_PARTICIPANT("Play media from end of last record for participant");
		
		String val = "";
		
		private MediaStart(String val) {
			this.val = val;
		}
		
		@Override
		public String toString() {
			return this.val;
		}
		
		public static MediaStart fromString(String value) {
			for(MediaStart v:values()) {
				if(v.toString().equals(value))
					return v;
			}
			return null;
		}
		
	}

	private ActionMap actionMap = new ActionMap();
	private InputMap inputMap = new InputMap();
	
	private SegmentationMode segmentationMode = SegmentationMode.INSERT_AT_END;
	
	private MediaStart mediaStart = MediaStart.AT_END_OF_LAST_RECORD;
	
	private Participant participantForMediaStart = Participant.UNKNOWN;
	
	private volatile boolean repaintEntireInterval = false;
	
	private long cachedSegmentWindow = 0L;
	
	private SegmentationWindow window = new SegmentationWindow();
	
	private final SessionEditor editor;
	
	private final SegmentationAWTEventListener segmentationListener = new SegmentationAWTEventListener();
	
	private final long segmentationEventMask = AWTEvent.KEY_EVENT_MASK;
	
	private Timer intervalTimer;
	
	private SegmentationIntervalTimerTask intervalTimerTask;
	
	private TimeUIModel.Interval segmentationInterval;
	
	private final static int MAX_BEEPS = 3;
	
	// number of times an invalid keypress has occured in a row
	// if this number hits MAX_BEEPS a dialog explaining we
	// are still in segmentation mode will be displayed
	private int beepCount = 0;
	
	public SegmentationHandler(SessionEditor editor) {
		super();
		this.editor = editor;
	
		setupActions();
	}
	
	private void setupActions() {
		// add actions for new records
		final String unidentifiedSegmentKey = "segment_unidentified";
		PhonUIAction newUnidentifiedSegmentAction = new PhonUIAction(this, "newSegment");
		actionMap.put(unidentifiedSegmentKey, newUnidentifiedSegmentAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), unidentifiedSegmentKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0), unidentifiedSegmentKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), unidentifiedSegmentKey);

		for(int i = 1; i <= 9; i++) {
			final String speakerSegmentKey = String.format("segment_speaker%d", i);
			PhonUIAction newSegmentAct = new PhonUIAction(this, "newSegment", i-1);
			actionMap.put(speakerSegmentKey, newSegmentAct);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0), speakerSegmentKey);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + i, 0), speakerSegmentKey);
		}
		
		// segmentation window
		final String toggleWindowKey = "toggle_segmentation_window";
		final PhonUIAction toggleWindowAct = new PhonUIAction(this, "onToggleSegmentationWindow");
		actionMap.put(toggleWindowKey, toggleWindowAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), toggleWindowKey);
		
		final String increaseWindowKey = "increase_segmentation_window";
		final PhonUIAction increaseWindowAct = new PhonUIAction(this, "onIncreaseSegmentationWindow");
		actionMap.put(increaseWindowKey, increaseWindowAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), increaseWindowKey);
		
		final String decreaseWindowKey = "decrease_segmentation_window";
		final PhonUIAction decreaseWindowAct = new PhonUIAction(this, "onDecreaseSegmentationWindow");
		actionMap.put(decreaseWindowKey, decreaseWindowAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), decreaseWindowKey);
		
		// mark breaks
		final String markBreakKey = "mark_break";
		final PhonUIAction markBreakAct = new PhonUIAction(this, "onMarkBreak");
		actionMap.put(markBreakKey, markBreakAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), markBreakKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0), markBreakKey);
		
		// media controls
		final String volumeUpKey = "volume_up";
		final PhonUIAction volumeUpAct = new PhonUIAction(this, "onVolumeUp");
		actionMap.put(volumeUpKey, volumeUpAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), volumeUpKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), volumeUpKey);
		
		final String volumeDownKey = "volume_down";
		final PhonUIAction volumeDownAct = new PhonUIAction(this, "onVolumeDown");
		actionMap.put(volumeDownKey, volumeDownAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), volumeDownKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0), volumeDownKey);

		final String increasePlaybackRateKey = "increase_playback_rate";
		final PhonUIAction increasePlaybackRateAct = new PhonUIAction(this, "onIncreasePlaybackRate");
		actionMap.put(increasePlaybackRateKey, increasePlaybackRateAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0), increasePlaybackRateKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), increasePlaybackRateKey);

		final String decreasePlaybackRateKey = "decrease_playback_rate";
		final PhonUIAction decreasePlaybackRateAct = new PhonUIAction(this, "onDecreasePlaybackRate");
		actionMap.put(decreasePlaybackRateKey, decreasePlaybackRateAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0), decreasePlaybackRateKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), decreasePlaybackRateKey);
		
		final String goBack1SKey = "goback_1s";
		final PhonUIAction goBack1SAct = new PhonUIAction(this, "onGoBack", 1000L);
		actionMap.put(goBack1SKey, goBack1SAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), goBack1SKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), goBack1SKey);
		
		final String goForward1SKey = "goforward_1s";
		final PhonUIAction goForward1SAct = new PhonUIAction(this, "onGoForward", 1000L);
		actionMap.put(goForward1SKey, goForward1SAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), goForward1SKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), goForward1SKey);
		
		final String goBack5SKey = "goback_5s";
		final PhonUIAction goBack5SAct = new PhonUIAction(this, "onGoBack", 5000L);
		actionMap.put(goBack5SKey, goBack5SAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), goBack5SKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.SHIFT_DOWN_MASK), goBack5SKey);
		
		final String goForward5SKey = "goforward_5s";
		final PhonUIAction goForward5SAct = new PhonUIAction(this, "onGoForward", 5000L);
		actionMap.put(goForward5SKey, goForward5SAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), goForward5SKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.SHIFT_DOWN_MASK), goForward5SKey);
		
		final String stopSegmentationKey = "stop_segmentation";
		PhonUIAction stopSegmentationAction = new PhonUIAction(this, "stopSegmentation");
		actionMap.put(stopSegmentationKey, stopSegmentationAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), stopSegmentationKey);
	}
	
	public InputMap getInputMap() {
		return this.inputMap;
	}
	
	public ActionMap getActionMap() {
		return this.actionMap;
	}
	
	public SegmentationMode getSegmentationMode() {
		return segmentationMode;
	}

	public void setSegmentationMode(SegmentationMode segmentationMode) {
		this.segmentationMode = segmentationMode;
	}
	
	public Participant getParticipantForMediaStart() {
		return participantForMediaStart;
	}

	public void setParticipantForMediaStart(Participant participantForMediaStart) {
		this.participantForMediaStart = participantForMediaStart;
	}

	public MediaStart getMediaStart() {
		return mediaStart;
	}

	public void setMediaStart(MediaStart mediaStart) {
		this.mediaStart = mediaStart;
	}

	public SegmentationWindow getWindow() {
		return window;
	}

	public void setWindow(SegmentationWindow window) {
		this.window = window;
	}

	/* Actions */
	public void newSegment(PhonActionEvent pae) {
		Participant speaker = null;
		
		// speaker index is determined by the 'Time Grid' view
		TimelineView tgView = (TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
		if(tgView != null) {
			if(pae.getData() != null) {
				int speakerIdx = (int)pae.getData();
				
				// check speaker index
				if(speakerIdx < 0 || speakerIdx >= tgView.getRecordTier().getSpeakerList().size()) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				
				speaker = editor.getSession().getParticipant(speakerIdx);
			}			
		}
		
		
		if(intervalTimerTask != null) {
			long playbackPosition = intervalTimerTask.currentSegmentationPosition();
					
			long segmentStart = window.getWindowStartMs(playbackPosition);
			long segmentEnd = window.getWindowEndMs(playbackPosition);
			
			if(segmentEnd - segmentStart < MIN_SEGMENT_LENGTH)
				return;
			
			long segmentOffset = 1;
			if(editor.getViewModel().isShowing(TimelineView.VIEW_TITLE)) {
				TimelineView timelineView = (TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
				float singlePxTime = timelineView.getTimeModel().timeAtX(timelineView.getTimeModel().getTimeInsets().left + 1);
				segmentOffset = (long)Math.ceil(singlePxTime * 1000.0f);
			}
			window.setStartLockMs(segmentEnd+segmentOffset);
			
			final SessionFactory factory = SessionFactory.newFactory();
			Record utt = factory.createRecord();
			
			MediaSegment m = factory.createMediaSegment();
			m.setStartValue(segmentStart);
			m.setEndValue(segmentEnd);
			
			// setup orthography
			utt.addGroup();
			utt.getOrthography().setGroup(0, new Orthography());
			utt.getSegment().setGroup(0, m);

			SegmentationMode mode = getSegmentationMode();
			if(mode == SegmentationMode.REPLACE_CURRENT && editor.currentRecord() == null) {
				// switch to 'add record' mode
				mode = SegmentationMode.INSERT_AT_END;
			}
			
			if(mode == SegmentationMode.REPLACE_CURRENT) {
				final CompoundEdit edit = new CompoundEdit() {

					@Override
					public String getUndoPresentationName() {
						return "Undo replace segment";
					}

					@Override
					public String getRedoPresentationName() {
						return "Redo replace segment";
					}

				};

				// don't replace speaker if no speaker was defined
				if(speaker != null) {
					final ChangeSpeakerEdit speakerEdit = new ChangeSpeakerEdit(editor, editor.currentRecord(), speaker);
					speakerEdit.doIt();
					edit.addEdit(speakerEdit);
				}

				final TierEdit<MediaSegment> segEdit = new TierEdit<MediaSegment>(editor, editor.currentRecord().getSegment(), 0, m);
				segEdit.doIt();
				edit.addEdit(segEdit);

				edit.end();
				editor.getUndoSupport().postEdit(edit);

				// move to next record (if available)
				int idx = editor.getCurrentRecordIndex() + 1;
				if(idx < editor.getDataModel().getRecordCount()) {
					editor.setCurrentRecordIndex(idx);
				} else {
					// switch modes
					setSegmentationMode(SegmentationMode.INSERT_AT_END);
				}
			} else {
				// setup speaker
				utt.setSpeaker((speaker != null ? speaker : Participant.UNKNOWN));

				int idx = editor.getDataModel().getRecordCount();
				// where are we going to insert
				if(mode == SegmentationMode.INSERT_AFTER_CURRENT) {
					idx = (editor.getSession().getRecordCount() == 0 ? 0 : editor.getCurrentRecordIndex() + 1);
				}
				final AddRecordEdit edit = new AddRecordEdit(editor, utt, idx);
				editor.getUndoSupport().postEdit(edit);
			}
		}
	}
	
	private final MediaPlayerEventListener mediaTimeListener = new MediaPlayerEventAdapter() {
		
		@Override
		public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			if(intervalTimerTask != null) {
				// re-sync time with media player
				intervalTimerTask.segmentsationSystemStartTime = System.currentTimeMillis();
				intervalTimerTask.segmentationMediaStartTime = newTime;
			}
		}

		@Override
		public void paused(MediaPlayer mediaPlayer) {
			SwingUtilities.invokeLater( () -> stopSegmentation());
		}
		
	};
	
	private class SegmentationIntervalTimerTask extends TimerTask {

		volatile long segmentsationSystemStartTime;
		
		volatile long segmentationMediaStartTime = 0L;
		
		@Override
		public void run() {
			if(segmentationInterval != null) {
				long currentTime = System.currentTimeMillis();
				long newTime = segmentationMediaStartTime + Math.round((currentTime - segmentsationSystemStartTime) * editor.getMediaModel().getPlaybackRate());
				long segStart = window.getWindowStartMs(newTime);
				long segEnd = window.getWindowEndMs(newTime);

				float prevStart = segmentationInterval.getStartMarker().getTime();
				float prevEnd = segmentationInterval.getEndMarker().getTime();

				// indicate we are going to change multiple values
				// avoid repainting until we say
				segmentationInterval.setValueAdjusting(true);
				segmentationInterval.getStartMarker().setTime(segStart / 1000.0f);
				segmentationInterval.getEndMarker().setTime(segEnd / 1000.0f);
				segmentationInterval.setValueAdjusting(false);

				if (editor.getViewModel().isShowing(TimelineView.VIEW_TITLE)) {
					TimelineView timelineView =
							(TimelineView) editor.getViewModel().getView(TimelineView.VIEW_TITLE);

					// autoscroll if necessary
					Rectangle visibleRect = timelineView.getRecordTier().getRecordGrid().getVisibleRect();
					float segMid = (segStart + ((segEnd - segStart) / 2));
					float timeAtCenter = timelineView.getTimeModel().timeAtX(visibleRect.getCenterX());

					if (((segMid / 1000.0f) > timeAtCenter) &&
							(timelineView.getWindowEnd() < timelineView.getTimeModel().getEndTime())) {
						float scrollToTime = Math.round(timelineView.getWindowStart() * 1000.0f + (segMid - (timeAtCenter * 1000.0f))) / 1000.0f;
						//timelineView.scrollToTime(segStart/1000.0f);
						timelineView.scrollToTime(scrollToTime);
					} else if ((segStart / 1000.0f) < timelineView.getWindowStart()) {
						timelineView.scrollToTime(segStart / 1000.0f);
					} else {
						// repaint interval (with time limit)
						long tn = (long) (1 / 60.0f * 1000.0f);
						if (repaintEntireInterval) {
							timelineView.getWaveformTier().getWaveformDisplay().repaint();
							repaintEntireInterval = false;
						} else {
							float s1 = Math.min(prevStart, segmentationInterval.getStartMarker().getTime());
							float e1 = Math.max(prevStart, segmentationInterval.getStartMarker().getTime());
							if (e1 - s1 > 0.0f) {
								timelineView.getTimebar().repaint(tn, s1, e1);
								timelineView.getWaveformTier().repaint(tn, s1, e1);
							}

							float s2 = Math.min(prevEnd, segmentationInterval.getEndMarker().getTime());
							float e2 = Math.max(prevEnd, segmentationInterval.getEndMarker().getTime());
							if (e2 - s2 > 0.0f) {
								timelineView.getTimebar().repaint(tn, s2, e2);
								timelineView.getWaveformTier().repaint(tn, s2, e2);
							}
						}
					}

					// special case: segmenting with no media
					//  update time model as we progress
					if (!editor.getMediaModel().isSessionMediaAvailable()) {
						float newEndTime = segEnd / 1000.0f;
						if (timelineView.getTimeModel().getEndTime() < newEndTime) {
							timelineView.getTimeModel().setEndTime(newEndTime);
						}
					}
				}
			}
		}
		
		long currentSegmentationPosition() {
			long currentTime = System.currentTimeMillis();
			long newTime = segmentationMediaStartTime + (currentTime - segmentsationSystemStartTime);
			return newTime;
		}
		
	}
	
	public void startSegmentation() {
		editor.getEventManager().queueEvent(new EditorEvent(EDITOR_SEGMENTATION_START));
		
		cachedSegmentWindow = (window.getBackwardWindowLengthMs() > 0 ? window.getBackwardWindowLengthMs() : 3000L);
		
		// add event listener
		Toolkit.getDefaultToolkit().addAWTEventListener(segmentationListener, segmentationEventMask);
		
		TimelineView timelineView = 
				(TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
		if(timelineView != null) {
			segmentationInterval = timelineView.getTimeModel().addInterval(0.0f, 0.0f);
			segmentationInterval.setOwner(timelineView.getWaveformTier().getWaveformDisplay());
			segmentationInterval.setRepaintOnTimeChange(false);
			segmentationInterval.setColor(UIManager.getColor(TimelineViewColors.SEGMENTATION_INTERVAL_BACKGROUND));
		}
		
		// start media playback
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			if(mediaStart == MediaStart.AT_BEGNINNING) {
				mediaView.getPlayer().setTime(0L);
			} else if(mediaStart == MediaStart.AT_END_OF_LAST_RECORD) {
				(new GoToEndOfSegmentedAction(editor, mediaView)).actionPerformed(new ActionEvent(this, 0, null));
				window.setStartLockMs(mediaView.getPlayer().getTime());
			} else if(mediaStart == MediaStart.FROM_CURRENT_POSITION) {
				window.setStartLockMs(mediaView.getPlayer().getTime());
			} else if(mediaStart == MediaStart.AT_END_OF_LAST_RECORD_FOR_PARTICIPANT) {
				(new GoToEndOfSegmentedAction(editor, mediaView, getParticipantForMediaStart())).actionPerformed(new ActionEvent(this, 0, null));
				window.setStartLockMs(mediaView.getPlayer().getTime());
			}
			
			intervalTimerTask = new SegmentationIntervalTimerTask();
			intervalTimerTask.segmentationMediaStartTime = mediaView.getPlayer().getTime();
			intervalTimerTask.segmentsationSystemStartTime = System.currentTimeMillis();
			
			// setup timer for 30fps
			intervalTimer = new Timer(true);
			intervalTimer.schedule(intervalTimerTask, 0L, (long)(1/30.0f * 1000.0f));
			
			// scroll to current media time
			timelineView.scrollToTime(mediaView.getPlayer().getTime() / 1000.0f);
			
			mediaView.getPlayer().addMediaPlayerListener(mediaTimeListener);
			if(!mediaView.getPlayer().isPlaying())
				mediaView.getPlayer().play();
		}
	}
	
	public void stopSegmentation() {
		editor.getEventManager().queueEvent(new EditorEvent(EDITOR_SEGMENTATION_END));
		
		Toolkit.getDefaultToolkit().removeAWTEventListener(segmentationListener);
		
		TimelineView timelineView = 
				(TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
		if(timelineView != null) {
			timelineView.getTimeModel().removeInterval(segmentationInterval);
			segmentationInterval = null;
		}
		
		if(intervalTimer != null) {
			intervalTimer.cancel();
		}
		
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			mediaView.getPlayer().removeMediaPlayerListener(mediaTimeListener);
			if(mediaView.getPlayer().isPlaying())
				mediaView.getPlayer().pause();
		}
	}
	
	public void onMarkBreak() {
		if(intervalTimerTask != null) {
			long playbackPosition = intervalTimerTask.currentSegmentationPosition();
			window.setStartLockMs(playbackPosition);
			
			TimelineView timelineView = 
					(TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
			if(timelineView != null && window.getBackwardWindowLengthMs() == 0L) {
				timelineView.repaint(timelineView.getVisibleRect());
			}
		}
	}

	public void onIncreasePlaybackRate() {
		float currentPlaybackRate = editor.getMediaModel().getPlaybackRate();
		if(currentPlaybackRate < 2.0f) {
			currentPlaybackRate += 0.25f;
			editor.getMediaModel().setPlaybackRate(currentPlaybackRate);
		}
	}

	public void onDecreasePlaybackRate() {
		float currentPlaybackRate = editor.getMediaModel().getPlaybackRate();
		if(currentPlaybackRate > 0.25f) {
			currentPlaybackRate -= 0.25f;
			editor.getMediaModel().setPlaybackRate(currentPlaybackRate);
		}
	}
	
	public void onVolumeUp() {
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			int currentVolume = mediaView.getPlayer().getVolume();
			int newVolume = Math.min(PhonMediaPlayer.VOL_MAX, currentVolume + VOLUME_INCR);
			mediaView.getPlayer().setVolume(newVolume);
		}
	}
	
	public void onVolumeDown() {
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			int currentVolume = mediaView.getPlayer().getVolume();
			int newVolume = Math.max(0, currentVolume - VOLUME_INCR);
			mediaView.getPlayer().setVolume(newVolume);
		}
	}
	
	public void onToggleSegmentationWindow() {
		long currentWindowLength = window.getBackwardWindowLengthMs();
		if(currentWindowLength == 0L) {
			window.setBackwardWindowLengthMs(cachedSegmentWindow);
		} else {
			cachedSegmentWindow = currentWindowLength;
			window.setBackwardWindowLengthMs(0L);
		}
		repaintEntireInterval = true;
	}
	
	public void onIncreaseSegmentationWindow() {
		long currentWindowLength = window.getBackwardWindowLengthMs();
		if(currentWindowLength > 0L) {
			long newWindowLength = Math.min(MAX_SEGMENT_WINDOW, currentWindowLength + SEGMENT_WINDOW_DELTA);
			window.setBackwardWindowLengthMs(newWindowLength);
		}
	}
	
	public void onDecreaseSegmentationWindow() {
		long currentWindowLength = window.getBackwardWindowLengthMs();
		if(currentWindowLength > 0L) {
			long newWindowLength = Math.max(MIN_SEGMENT_WINDOW, currentWindowLength - SEGMENT_WINDOW_DELTA);
			window.setBackwardWindowLengthMs(newWindowLength);
		}
	}
	
	public void onGoBack(PhonActionEvent pae) {
		long deltaMs = (long)pae.getData();
		
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			long newTime = Math.max(Math.max(0, window.getStartLockMs()), mediaView.getPlayer().getTime() - deltaMs);
			mediaView.getPlayer().setTime(newTime);
		}
	}
	
	public void onGoForward(PhonActionEvent pae) {
		long deltaMs = (long)pae.getData();
		
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			long newTime = Math.min(mediaView.getPlayer().getLength(), mediaView.getPlayer().getTime() + deltaMs);
			mediaView.getPlayer().setTime(newTime);
		}
	}
	
	private void handleKeyEvent(KeyEvent ke) {
		if(ke.getID() == KeyEvent.KEY_PRESSED) {
			var ks = KeyStroke.getKeyStrokeForEvent(ke);
			var actionKey = inputMap.get(ks);
			if(actionKey != null) {
				var action = actionMap.get(actionKey);
				if(action.accept(this)) {
					action.actionPerformed(new ActionEvent(this, 0, actionKey.toString()));
				}
			} else {
				// don't beep on meta-key press
//				if(ke.isActionKey()) {
					++beepCount;
					Toolkit.getDefaultToolkit().beep();
					
					// TODO improve message in dialog
					if(beepCount >= MAX_BEEPS) {
						final MessageDialogProperties props = new MessageDialogProperties();
						props.setParentWindow(editor);
						props.setRunAsync(true);
						props.setTitle("Segmentation Mode");
						props.setHeader("Segmentation Mode");
						props.setMessage("Editor is in segmentation mode.");
						props.setOptions(new String[] { "Continue", "End Segmentation" } );
						props.setListener(beepDialogListener);
						NativeDialogs.showMessageDialog(props);
					}
//				}
			}
		}
		
		ke.consume();
	}
	
	private final NativeDialogListener beepDialogListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent arg0) {
			if(arg0.getDialogResult() == 1) {
				stopSegmentation();
			}
			beepCount = 0;
		}
		
	};
	
	private void handleMouseEvent(MouseEvent me) {
		
	}
		
	private class SegmentationAWTEventListener implements AWTEventListener {
		
		@Override
		public void eventDispatched(AWTEvent event) {
			if(event instanceof MouseEvent) {
				handleMouseEvent((MouseEvent)event);
			} else if(event instanceof KeyEvent) {
				handleKeyEvent((KeyEvent)event);
			}
		}
		
	}

}
