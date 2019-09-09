package ca.phon.app.session.editor.view.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import com.teamdev.jxbrowser.chromium.internal.ipc.message.SetupProtocolHandlerMessage;

import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.TimeUIModel.Marker;
import ca.phon.app.log.LogUtil;
import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.DeleteRecordAction;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.media_player.actions.PlaySegmentAction;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.app.session.editor.view.session_information.actions.NewParticipantAction;
import ca.phon.app.session.editor.view.timeline.RecordGrid.GhostMarker;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;

public class TimelineRecordTier extends TimelineTier {
	
	private RecordGrid recordGrid;
	
	private Map<Participant, Boolean> speakerVisibility = new HashMap<>();
	
	private Map<String, Boolean> tierVisibility = new HashMap<>();
	
	private TimeUIModel.Interval currentRecordInterval = null;
	
	public TimelineRecordTier(TimelineView parent) {
		super(parent);
	
		init();
		setupEditorEvents();
	}

	private void init() {
		Session session = getParentView().getEditor().getSession();
		recordGrid = new RecordGrid(getTimeModel(), session);
		if(getParentView().getEditor().currentRecord() != null)
			setupRecord(getParentView().getEditor().currentRecord());
		
		recordGrid.setFont(FontPreferences.getTierFont());
		setupRecordGridActions();
		setupSpeakers();
		
		// add ortho by default
		tierVisibility.put(SystemTierType.Orthography.getName(), Boolean.TRUE);
		tierVisibility.put(SystemTierType.Segment.getName(), Boolean.TRUE);
		setupTiers();
		
		recordGrid.addRecordGridMouseListener(mouseListener);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(recordGrid, BorderLayout.CENTER);
	}
	
	public RecordGrid getRecordGrid() {
		return this.recordGrid;
	}
	
	private void setupRecordGridActions() {
		final InputMap inputMap = recordGrid.getInputMap();
		final ActionMap actionMap = recordGrid.getActionMap();
		
		final String deleteRecordKey = "delete_record";
		final DeleteRecordAction deleteRecordAction = new DeleteRecordAction(getParentView().getEditor());
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteRecordKey);
		actionMap.put(deleteRecordKey, deleteRecordAction);
		
		final String playSegmentKey = "play_segment";
		final PhonUIAction playSegmentAction = new PhonUIAction(this, "onPlaySegment");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), playSegmentKey);
		actionMap.put(playSegmentKey, playSegmentAction);
		
		recordGrid.setInputMap(WHEN_FOCUSED, inputMap);
		recordGrid.setActionMap(actionMap);
	}
	
	public void onPlaySegment(PhonActionEvent pae) {
		if(getParentView().getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			MediaPlayerEditorView mediaView =
					(MediaPlayerEditorView)getParentView().getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE);
			PlaySegmentAction playSegmentAction = new PlaySegmentAction(getParentView().getEditor(), mediaView);
			playSegmentAction.actionPerformed(pae.getActionEvent());
		}
	}
	
	private final DelegateEditorAction onRecordChange = 
			new DelegateEditorAction(this, "onRecordChange");
	
	private final DelegateEditorAction onSpeakerChange = 
			new DelegateEditorAction(this, "onSpeakerChange");
		
	private final DelegateEditorAction onTierChangedAct = 
			new DelegateEditorAction(this, "onTierChanged");
	
	private final DelegateEditorAction onParticipantRemoveAct =
			new DelegateEditorAction(this, "onParticipantRemoved");
	
	private final DelegateEditorAction onParticipantAddedAct =
			new DelegateEditorAction(this, "onParticipantAdded");
	
	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.SPEAKER_CHANGE_EVT, onSpeakerChange);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantRemoveAct);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
	}
	
	private void deregisterEditorEvents() {
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.SPEAKER_CHANGE_EVT, onSpeakerChange);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantRemoveAct);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
	}
	
	private void setupRecord(Record r) {
		if(currentRecordInterval != null)
			getTimeModel().removeInterval(currentRecordInterval);
		
		MediaSegment segment = r.getSegment().getGroup(0);
		var segStartTime = segment.getStartValue() / 1000.0f;
		var segEndTime = segment.getEndValue() / 1000.0f;
		
		// check for 'GhostMarker's which, if present, will
		// become the start/end marker for the record interval
		Optional<RecordGrid.GhostMarker> ghostMarker = 
				recordGrid.getTimeModel().getMarkers().parallelStream()
					.filter( (m) -> m instanceof GhostMarker )
					.map( (m) -> GhostMarker.class.cast(m) )
					.findAny();
		if(ghostMarker.isPresent()) {
			Marker startMarker = ghostMarker.get().isStart() ? ghostMarker.get() : new Marker(segStartTime);
			Marker endMarker = ghostMarker.get().isStart() ? new Marker(segEndTime) : ghostMarker.get();
			currentRecordInterval = getTimeModel().addInterval(startMarker, endMarker);
			currentRecordInterval.setRepaintEntireInterval(true);
			currentRecordInterval.addPropertyChangeListener(new RecordIntervalListener());
			currentRecordInterval.setValueAdjusting(true);
			ghostMarker.get().addPropertyChangeListener( "valueAdjusting", (e) -> {
				currentRecordInterval.setValueAdjusting((boolean)e.getNewValue());
			});
		} else {
			currentRecordInterval = getTimeModel().addInterval(segStartTime, segEndTime);
			currentRecordInterval.setRepaintEntireInterval(true);
			currentRecordInterval.addPropertyChangeListener(new RecordIntervalListener());
		}
		
		recordGrid.setCurrentRecord(r);
	}
		
	/* Editor events */
	@RunOnEDT
	public void onRecordChange(EditorEvent evt) {
		Record r = (Record)evt.getEventData();
		setupRecord(r);
		
		recordGrid.repaint(recordGrid.getVisibleRect());
	}
	
	@RunOnEDT
	public void onSpeakerChange(EditorEvent evt) {
		recordGrid.repaint(recordGrid.getVisibleRect());
	}
	
	@RunOnEDT
	public void onParticipantRemoved(EditorEvent ee) {
		speakerVisibility.remove((Participant)ee.getEventData());
		setupSpeakers();
	}
	
	@RunOnEDT
	public void onParticipantAdded(EditorEvent ee) {
		setupSpeakers();
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if(SystemTierType.Orthography.getName().equals(ee.getEventData().toString())
				|| SystemTierType.Segment.getName().equals(ee.getEventData().toString())) {
			setupRecord(getParentView().getEditor().currentRecord());
			recordGrid.repaint(recordGrid.getVisibleRect());
		}
	}
	
	/**
	 * Is the speaker visible? 
	 * 
	 * @param speaker
	 * @return
	 */
	public boolean isSpeakerVisible(Participant speaker) {
		boolean retVal = true;
		
		if(speakerVisibility.containsKey(speaker))
			retVal = speakerVisibility.get(speaker);
		
		return retVal;
	}
	
	public void setSpeakerVisible(Participant speaker, boolean visible) {
		speakerVisibility.put(speaker, visible);
		setupSpeakers();
	}
	
	public void toggleSpeaker(PhonActionEvent pae) {
		Participant speaker = (Participant)pae.getData();
		setSpeakerVisible(speaker, !isSpeakerVisible(speaker));
	}
	
	public List<Participant> getSpeakerList() {
		List<Participant> retVal = new ArrayList<>();
		
		Session session = getParentView().getEditor().getSession();
		for(var speaker:session.getParticipants()) {
			if(isSpeakerVisible(speaker)) {
				retVal.add(speaker);
			}
		}
		
		return retVal;
	}
	
	private void setupSpeakers() {
		Session session = getParentView().getEditor().getSession();
		
		var speakerList = new ArrayList<Participant>();
		for(var speaker:session.getParticipants()) {
			if(isSpeakerVisible(speaker))
				speakerList.add(speaker);
		}
		if(isSpeakerVisible(Participant.UNKNOWN))
			speakerList.add(Participant.UNKNOWN);
		recordGrid.setSpeakers(speakerList);
	}
	
	public boolean isTierVisible(String tierName) {
		boolean retVal = false;
		
		if(tierVisibility.containsKey(tierName)) 
			retVal = tierVisibility.get(tierName);
		
		return retVal;
	}
	
	public void setTierVisible(String tierName, boolean visible) {
		tierVisibility.put(tierName, visible);
		setupTiers();
	}
	
	public void toggleTier(String tierName) {
		setTierVisible(tierName, !isTierVisible(tierName));
	}
	
	private void setupTiers() {
		Session session = getParentView().getEditor().getSession();
		recordGrid.setTiers(
				session.getTierView().stream()
					.map( TierViewItem::getTierName )
					.filter( this::isTierVisible )
					.collect( Collectors.toList() )
		);
	}
	
	public void setupContextMenu(MenuBuilder builder) {
		setupSpeakerMenu(builder);
	}
	
	public void setupSpeakerMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for(var speaker:session.getParticipants()) {
			final PhonUIAction toggleSpeakerAct = new PhonUIAction(this, "toggleSpeaker", speaker);
			toggleSpeakerAct.putValue(PhonUIAction.NAME, speaker.toString());
			toggleSpeakerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle speaker " + speaker);
			toggleSpeakerAct.putValue(PhonUIAction.SELECTED_KEY, isSpeakerVisible(speaker));
			final JCheckBoxMenuItem toggleSpeakerItem = new JCheckBoxMenuItem(toggleSpeakerAct);
			builder.addItem(".", toggleSpeakerItem);
		}
		
		final PhonUIAction toggleUnknownAct = new PhonUIAction(this, "toggleSpeaker", Participant.UNKNOWN);
		toggleUnknownAct.putValue(PhonUIAction.NAME, Participant.UNKNOWN.toString());
		toggleUnknownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle speaker " + Participant.UNKNOWN);
		toggleUnknownAct.putValue(PhonUIAction.SELECTED_KEY, isSpeakerVisible(Participant.UNKNOWN));
		final JCheckBoxMenuItem toggleUnknownItem = new JCheckBoxMenuItem(toggleUnknownAct);
		builder.addItem(".", toggleUnknownItem);
		
		builder.addSeparator(".", "speaker_actions");
		
		final NewParticipantAction newParticipantAct = new NewParticipantAction(getParentView().getEditor(),
				(SessionInfoEditorView)getParentView().getEditor().getViewModel().getView(SessionInfoEditorView.VIEW_TITLE));
		builder.addItem(".", newParticipantAct);
	}
	
	public void setupTierMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for(var tierViewItem:session.getTierView()) {
			final PhonUIAction toggleTierAct = new PhonUIAction(this, "toggleTier", tierViewItem.getTierName());
			toggleTierAct.putValue(PhonUIAction.NAME, tierViewItem.getTierName());
			toggleTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle tier " + tierViewItem.getTierName());
			toggleTierAct.putValue(PhonUIAction.SELECTED_KEY, isTierVisible(tierViewItem.getTierName()));
			final JCheckBoxMenuItem toggleTierItem = new JCheckBoxMenuItem(toggleTierAct);
			builder.addItem(".", toggleTierItem);
		}
	}

	
	public boolean isResizeable() {
		return false;
	}

	@Override
	public void onClose() {
		deregisterEditorEvents();
	}
	
	private class RecordIntervalListener implements PropertyChangeListener {
		
		private boolean isFirstChange = true;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Record r = recordGrid.getCurrentRecord();
			MediaSegment segment = r.getSegment().getGroup(0);
			final SessionFactory factory = SessionFactory.newFactory();
			
			if(evt.getPropertyName().equals("valueAdjusting")) {
				if((boolean)evt.getNewValue()) {
					isFirstChange = true;
					getParentView().getEditor().getUndoSupport().beginUpdate();
				} else {
					getParentView().getEditor().getUndoSupport().endUpdate();
				}
			} else {
				if(!evt.getPropertyName().endsWith("time")) return;
				
				MediaSegment newSegment = factory.createMediaSegment();
				newSegment.setStartValue(segment.getStartValue());
				newSegment.setEndValue(segment.getEndValue());
				
				if(evt.getPropertyName().startsWith("startMarker")) {
					newSegment.setStartValue((float)evt.getNewValue() * 1000.0f);
				} else if(evt.getPropertyName().startsWith("endMarker")) {
					newSegment.setEndValue((float)evt.getNewValue() * 1000.0f);
				}
				
				TierEdit<MediaSegment> segmentEdit = new TierEdit<MediaSegment>(getParentView().getEditor(), r.getSegment(), 0, newSegment);
				getParentView().getEditor().getUndoSupport().postEdit(segmentEdit);
				segmentEdit.setFireHardChangeOnUndo(isFirstChange);
				isFirstChange = false;
			}
		}
		
	}
	
	
	
	private RecordGridMouseListener mouseListener = new RecordGridMouseAdapter() {

		private int currentDraggedRecord = -1;
		
		// offset (in sec) from left of interval where we are starting the drag
		private float mouseDragOffset = 0.0f;
		
		@Override
		public void recordClicked(int recordIndex, MouseEvent me) {
			getParentView().getEditor().setCurrentRecordIndex(recordIndex);
		}
		
		@Override
		public void recordPressed(int recordIndex, MouseEvent me) {
			
		}
		
		@Override
		public void recordReleased(int recordIndex, MouseEvent me) {
			if(currentDraggedRecord >= 0) {
				currentDraggedRecord = -1;
				if(currentRecordInterval != null)
					currentRecordInterval.setValueAdjusting(false);
			}
		}

		@Override
		public void recordDragged(int recordIndex, MouseEvent me) {
			if(getParentView().getEditor().getCurrentRecordIndex() != recordIndex) {
				getParentView().getEditor().setCurrentRecordIndex(recordIndex);
				return;
			} else {
				// shouldn't happen
				if(currentRecordInterval == null) return;
								
				if(currentDraggedRecord != recordIndex) {
					// don't adjust an already changing interval
					if(currentRecordInterval.isValueAdjusting()) return;
					currentDraggedRecord = recordIndex;
					currentRecordInterval.setValueAdjusting(true);

					mouseDragOffset = getTimeModel().timeAtX(me.getX()) -
							currentRecordInterval.getStartMarker().getTime();
				}

				Participant mouseOverSpeaker = recordGrid.getUI().getSpeakerAtPoint(me.getPoint());
				if(mouseOverSpeaker != null && mouseOverSpeaker != getParentView().getEditor().currentRecord().getSpeaker()) {
					// change speakers
					ChangeSpeakerEdit chEdit = new ChangeSpeakerEdit(getParentView().getEditor(), getParentView().getEditor().currentRecord(), mouseOverSpeaker);
					getParentView().getEditor().getUndoSupport().postEdit(chEdit);
				}
				
				float startTime = currentRecordInterval.getStartMarker().getTime();
				float endTime = currentRecordInterval.getEndMarker().getTime();
				float intervalDuration = endTime - startTime;

				float oldOffsetTime = currentRecordInterval.getStartMarker().getTime() + mouseDragOffset;
				float newOffsetTime = recordGrid.timeAtX(me.getX());
				int direction = (oldOffsetTime < newOffsetTime ? 1 : -1);
				
				float newStartTime = 0.0f;
				float newEndTime = 0.0f;
				
				if(direction < 0) {
					newStartTime = Math.max(newOffsetTime - mouseDragOffset, getTimeModel().getStartTime());
					newEndTime = newStartTime + intervalDuration;
				} else {
					newEndTime = Math.min(newOffsetTime + (intervalDuration-mouseDragOffset), getTimeModel().getEndTime());
					newStartTime = newEndTime - intervalDuration;
				}
				
				currentRecordInterval.getStartMarker().setTime(newStartTime);
				currentRecordInterval.getEndMarker().setTime(newEndTime);
			}
		}
		
	};
}
