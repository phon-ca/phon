package ca.phon.app.session.editor.view.timeline;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.undo.CompoundEdit;

import ca.phon.app.media.TimeComponent;
import ca.phon.app.media.TimeUIModel;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.media_player.actions.GoToEndOfSegmentedAction;
import ca.phon.orthography.Orthography;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;

public final class SegmentationHandler {

	/* Editor events */
	public final static String EDITOR_SEGMENTATION_START = "_segmentation_start_";
	
	public final static String EDITOR_SEGMENTATION_END = "_segmentation_end_";
	
	private final static long MIN_SEGMENT_LENGTH = 50L;
	
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
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), volumeUpKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), volumeUpKey);
		
		final String volumeDownKey = "volume_down";
		final PhonUIAction volumeDownAct = new PhonUIAction(this, "onVolumeDown");
		actionMap.put(volumeDownKey, volumeDownAct);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), volumeDownKey);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0), volumeDownKey);
		
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
			
			window.setStartLockMs(segmentEnd+1);
			
			final SessionFactory factory = SessionFactory.newFactory();
			Record utt = factory.createRecord();
			
			MediaSegment m = factory.createMediaSegment();
			m.setStartValue(segmentStart);
			m.setEndValue(segmentEnd);
			
			// setup orthography
			utt.getOrthography().addGroup(new Orthography());
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
				synchronized (intervalTimerTask) {					
					intervalTimerTask.segmentsationSystemStartTime = System.currentTimeMillis();
					intervalTimerTask.segmentationMediaStartTime = newTime;
				}
			}
		}

		@Override
		public void paused(MediaPlayer mediaPlayer) {

			stopSegmentation();
		}

		@Override
		public void stopped(MediaPlayer mediaPlayer) {
			stopSegmentation();
		}
		
	};
	
	private class SegmentationIntervalTimerTask extends TimerTask {

		volatile long segmentsationSystemStartTime;
		
		volatile long segmentationMediaStartTime = 0L;
		
		@Override
		public void run() {
			if(segmentationInterval != null) {
				long currentTime = System.currentTimeMillis();
				synchronized (this) {
					long newTime = segmentationMediaStartTime + (currentTime - segmentsationSystemStartTime);
					long segStart = window.getWindowStartMs(newTime);
					long segEnd = window.getWindowEndMs(newTime);
					
					// indicate we are going to change multiple values
					segmentationInterval.setValueAdjusting(true);
					segmentationInterval.getStartMarker().setTime(segStart/1000.0f);
					// indicate we are finished changing values
					segmentationInterval.setValueAdjusting(false);
					segmentationInterval.getEndMarker().setTime(segEnd/1000.0f);
					
					// Autoscroll 
					if(editor.getViewModel().isShowing(TimelineView.VIEW_TITLE)) {
						TimelineView timelineView = 
								(TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
						
						// special case: segmenting with no media
						//  update time model as we progress
						if(!editor.getMediaModel().isSessionMediaAvailable()) {
							float newEndTime = segEnd / 1000.0f;
							if(timelineView.getTimeModel().getEndTime() < newEndTime) {
								timelineView.getTimeModel().setEndTime(newEndTime);
							}
						}
						
						Rectangle visibleRect = timelineView.getRecordTier().getRecordGrid().getVisibleRect();
						if((segEnd/1000.0f) > timelineView.getTimeModel().timeAtX(visibleRect.getMaxX())) {
							timelineView.scrollToTime(segStart/1000.0f);
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
		
		// add event listener
		Toolkit.getDefaultToolkit().addAWTEventListener(segmentationListener, segmentationEventMask);
		
		TimelineView timelineView = 
				(TimelineView)editor.getViewModel().getView(TimelineView.VIEW_TITLE);
		if(timelineView != null) {
			segmentationInterval = timelineView.getTimeModel().addInterval(0.0f, 0.0f);
			segmentationInterval.setColor(new Color(255, 255, 0, 50));
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
		}
	}
	
	public void onVolumeUp() {
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
		}
	}
	
	public void onVolumeDown() {
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
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
