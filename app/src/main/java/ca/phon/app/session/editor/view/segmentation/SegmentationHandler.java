package ca.phon.app.session.editor.view.segmentation;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.undo.CompoundEdit;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.media_player.actions.GoToEndOfSegmentedAction;
import ca.phon.app.session.editor.view.segmentation.SegmentationEditorView.SegmentationMode;
import ca.phon.app.session.editor.view.timegrid.TimeGridView;
import ca.phon.orthography.Orthography;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.MsFormatter;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

public final class SegmentationHandler {

	/* Editor events */
	public final static String EDITOR_SEGMENTATION_START = "_segmentation_start_";
	
	public final static String EDITOR_SEGMENTATION_END = "_segmentation_end_";
	
	private final static long MIN_SEGMENT_LENGTH = 50L;
	
	public enum SegmentationMode {
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
	}
	
	public enum MediaStart {
		AT_BEGNINNING("Play media from beginning"),
		FROM_CURRENT_POSITION("From current position"),
		AT_END_OF_LAST_RECORD("Play media from end of last record");
		
		String val = "";
		
		private MediaStart(String val) {
			this.val = val;
		}
		
		@Override
		public String toString() {
			return this.val;
		}
	}

	private ActionMap actionMap = new ActionMap();
	private InputMap inputMap = new InputMap();
	
	private SegmentationMode segmentationMode = SegmentationMode.INSERT_AT_END;
	
	private MediaStart mediaStart = MediaStart.AT_END_OF_LAST_RECORD;
	
	private SegmentationWindow window = new SegmentationWindow();
	
	private final SessionEditor editor;
	
	private final SegmentationAWTEventListener segmentationListener = new SegmentationAWTEventListener();
	
	private final long segmentationEventMask = AWTEvent.KEY_EVENT_MASK;
	
	private TimeUIModel.Interval segmentationInterval;
	
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
		TimeGridView tgView = (TimeGridView)editor.getViewModel().getView(TimeGridView.VIEW_TITLE);
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
		
		
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			long playbackPosition = mediaView.getPlayer().getTime();
			
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
			if(segmentationInterval != null) {
				long segStart = window.getWindowStartMs(newTime);
				long segEnd = window.getWindowEndMs(newTime);
				segmentationInterval.getStartMarker().setTime(segStart/1000.0f);
				segmentationInterval.getEndMarker().setTime(segEnd/1000.0f);
			}
			
			TimeGridView timeGridView = (TimeGridView)editor.getViewModel().getView(TimeGridView.VIEW_TITLE);
			if(timeGridView != null) {
				Rectangle rect = timeGridView.getWaveformTier().getWaveformDisplay().getVisibleRect();
				float maxTimeS = timeGridView.getTimeModel().timeAtX(rect.getMaxX());
				if(newTime > (maxTimeS*1000.0)) {
					timeGridView.scrollToTime((float)(window.getStartLockMs()/1000.0f));
				}
			}
		}
		
	};
	
	public void startSegmentation() {
		editor.getEventManager().queueEvent(new EditorEvent(EDITOR_SEGMENTATION_START));
		
		// add event listener
		Toolkit.getDefaultToolkit().addAWTEventListener(segmentationListener, segmentationEventMask);
		
		TimeGridView timeGridView = 
				(TimeGridView)editor.getViewModel().getView(TimeGridView.VIEW_TITLE);
		if(timeGridView != null) {
			segmentationInterval = timeGridView.getTimeModel().addInterval(0.0f, 0.0f);
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
			}
			
			mediaView.getPlayer().addMediaPlayerListener(mediaTimeListener);
			if(!mediaView.getPlayer().isPlaying())
				mediaView.getPlayer().play();
		}
	}
	
	public void stopSegmentation() {
		editor.getEventManager().queueEvent(new EditorEvent(EDITOR_SEGMENTATION_END));
		
		Toolkit.getDefaultToolkit().removeAWTEventListener(segmentationListener);
		
		TimeGridView timeGridView = 
				(TimeGridView)editor.getViewModel().getView(TimeGridView.VIEW_TITLE);
		if(timeGridView != null) {
			timeGridView.getTimeModel().removeInterval(segmentationInterval);
			segmentationInterval = null;
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
		MediaPlayerEditorView mediaView = 
				(MediaPlayerEditorView)editor.getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaView != null) {
			long playbackPosition = mediaView.getPlayer().getTime();
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
//		if(!ke.isActionKey()) return;
		if(ke.getID() == KeyEvent.KEY_PRESSED) {
			var ks = KeyStroke.getKeyStrokeForEvent(ke);
			var actionKey = inputMap.get(ks);
			if(actionKey != null) {
				var action = actionMap.get(actionKey);
				if(action.accept(this)) {
					action.actionPerformed(new ActionEvent(this, 0, actionKey.toString()));
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		
		ke.consume();
	}
	
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
