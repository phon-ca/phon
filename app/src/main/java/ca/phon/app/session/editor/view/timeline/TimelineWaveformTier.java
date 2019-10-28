
package ca.phon.app.session.editor.view.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.WaveformDisplay;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.media_player.actions.PlaySegmentAction;
import ca.phon.orthography.Orthography;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;

public class TimelineWaveformTier extends TimelineTier  {

	private static final long serialVersionUID = -2864344329017995791L;

	private WaveformDisplay wavDisplay;
	
	public TimelineWaveformTier(TimelineView parent) {
		super(parent);
		
		init();
		setupWavformActions();
		setupEditorEvents();
	}
	
	private void init() {
		final TimeUIModel timeModel = getParentView().getTimeModel();
		
		wavDisplay = new WaveformDisplay(timeModel);
		Insets channelInsets = new Insets(wavDisplay.getChannelInsets().top, timeModel.getTimeInsets().left,
				wavDisplay.getChannelInsets().bottom, timeModel.getTimeInsets().right);
		wavDisplay.setChannelInsets(channelInsets);
		wavDisplay.setPreferredChannelHeight(50);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setBackground(Color.WHITE);
		wavDisplay.setOpaque(true);
		wavDisplay.addFocusListener(selectionFocusListener);
		
		wavDisplay.getPreferredSize();
		
		wavDisplay.addMouseListener(selectionListener);
		wavDisplay.addMouseMotionListener(selectionListener);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(wavDisplay, BorderLayout.CENTER);
	}
	
	private void setupWavformActions() {
		final InputMap inputMap = wavDisplay.getInputMap();
		final ActionMap actionMap = wavDisplay.getActionMap();
		
		final String playKey = "play";
		final PhonUIAction playAction = new PhonUIAction(this, "onPlay");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), playKey);
		actionMap.put(playKey, playAction);
		
		final String escapeKey = "escape";
		final PhonUIAction escapeAction = new PhonUIAction(this, "onEscape");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escapeKey);
		actionMap.put(escapeKey, escapeAction);
		
		// setup record creation actions
		for(int i = 0; i < 10; i++) {
			final PhonUIAction recordCreationAct = new PhonUIAction(this, "onCreateRecord", i);
			final KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0);
			final KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + i, 0);
			final String recordCreationId = "create_record_for_speaker_" + i;
			inputMap.put(ks1, recordCreationId);
			inputMap.put(ks2, recordCreationId);
			actionMap.put(recordCreationId, recordCreationAct);
		}
		
		wavDisplay.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
		wavDisplay.setActionMap(actionMap);
	}
	
	public WaveformDisplay getWaveformDisplay() {
		return this.wavDisplay;
	}
	
	private final DelegateEditorAction onRecordChange = 
			new DelegateEditorAction(this, "onRecordChange");
	
	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
	}
	
	public void toggleVisible() {
		setVisible(!isVisible());
	}
	
	@Override
	public void setupContextMenu(MouseEvent me, MenuBuilder builder) {
		final PhonUIAction toggleVisiblityAct = new PhonUIAction(this, "toggleVisible");
		toggleVisiblityAct.putValue(PhonUIAction.NAME, 
				(isVisible() ? "Hide waveform" : "Show waveform"));
		toggleVisiblityAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle waveform visibility");
		builder.addItem(".", toggleVisiblityAct);
	}
	
	/* UI Events */
	public void onPlay(PhonActionEvent pae) {
		if(selectionInterval != null) {
			// play selected interval
			if(getParentView().getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
				MediaPlayerEditorView mediaView =
						(MediaPlayerEditorView)getParentView().getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
				
				long startTime = (int)(selectionInterval.getStartMarker().getTime() * 1000.0f);
				long duration = ((int)(selectionInterval.getEndMarker().getTime() * 1000.0f)) - startTime;
				mediaView.getPlayer().playSegment(startTime, duration);
			}
		} else {
			// play from current position
		}
	}
	
	public void onEscape(PhonActionEvent pae) {
		if(getParentView().getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			MediaPlayerEditorView mediaView =
					(MediaPlayerEditorView)getParentView().getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			if(mediaView.getPlayer().isPlaying()) {
				mediaView.getPlayer().pause();
			} else {
				clearSelection();
			}
		} else if(selectionInterval != null) {
			clearSelection();
		}
	}
	
	public void onCreateRecord(PhonActionEvent pae) {
		if(selectionInterval == null) return;
		
		Participant speaker = Participant.UNKNOWN;
		int speakerNum = (int)pae.getData();
		if(speakerNum > 0) {
			List<Participant> speakerList = 
					getParentView().getRecordTier().getSpeakerList();
			if(speakerNum - 1 < speakerList.size()) {
				speaker = speakerList.get(speakerNum-1);
			}
		}
		
		final SessionFactory factory = SessionFactory.newFactory();
		Record utt = factory.createRecord();
		utt.setSpeaker(speaker);
		
		MediaSegment m = factory.createMediaSegment();
		m.setStartValue(selectionInterval.getStartMarker().getTime() * 1000.0f);
		m.setEndValue(selectionInterval.getEndMarker().getTime() * 1000.0f);
		
		utt.getOrthography().addGroup(new Orthography());
		utt.getSegment().setGroup(0, m);
		
		final SessionEditor editor = getParentView().getEditor();
		final AddRecordEdit edit = new AddRecordEdit(editor, utt);
		editor.getUndoSupport().postEdit(edit);
		
		clearSelection();
	}
	
	/* Editor Events */
	@RunOnEDT
	public void onRecordChange(EditorEvent ee) {
		wavDisplay.repaint(wavDisplay.getVisibleRect());
	}
	
	/* Selection using mouse */
	private Interval selectionInterval = null;
	
	private float initialSelectionTime = -1.0f;
	
	public Interval getSelection() {
		return this.selectionInterval;
	}
	
	public void clearSelection() {
		if(selectionInterval != null)
			getTimeModel().removeInterval(selectionInterval);
		selectionInterval = null;
	}
	
	private FocusListener selectionFocusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e) {
			if(selectionInterval != null) {
				selectionInterval.setColor(UIManager.getColor(TimelineViewColors.INTERVAL_BACKGROUND));
			}
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			if(selectionInterval != null) {
				selectionInterval.setColor(UIManager.getColor(TimelineViewColors.FOCUSED_INTERVAL_BACKGROUND));
			}
		}
		
	};
	
	private MouseInputAdapter selectionListener = new MouseInputAdapter() {

		@Override
		public void mousePressed(MouseEvent e) {
			wavDisplay.requestFocus();
			if(wavDisplay.getUI().getCurrentlyDraggedMarker() != null) {
				initialSelectionTime = -1.0f;
				return;
			}
			
			if(e.getButton() == MouseEvent.BUTTON1) {
				
				if(selectionInterval != null) {
					getTimeModel().removeInterval(selectionInterval);
					selectionInterval = null;
				}
				
				initialSelectionTime = getTimeModel().timeAtX(e.getX());
				
				// TODO change media playback position
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if(initialSelectionTime > 0) {
				float currentTime = getTimeModel().timeAtX(e.getX());
				float diff = currentTime - initialSelectionTime;
				if(selectionInterval == null) {
					float intervalStartTime, intervalEndTime = 0.0f;
					if(diff > 0) {
						intervalStartTime = initialSelectionTime;
						intervalEndTime = currentTime; 
						
					} else {
						intervalStartTime = currentTime;
						intervalEndTime = initialSelectionTime;
					}
					
					selectionInterval = getTimeModel().addInterval(intervalStartTime, intervalEndTime);
					selectionInterval.setColor(new Color(50, 125, 200, 50));
					
					if(diff > 0) {
						wavDisplay.getUI().beginDrag(selectionInterval, selectionInterval.getEndMarker());
					} else {
						wavDisplay.getUI().beginDrag(selectionInterval, selectionInterval.getStartMarker());
					}
				}
			}
		}
		
	};
		
}
