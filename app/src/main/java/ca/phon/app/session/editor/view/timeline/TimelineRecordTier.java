package ca.phon.app.session.editor.view.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

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
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.DeleteRecordEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.media_player.actions.PlaySegmentAction;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.app.session.editor.view.session_information.actions.NewParticipantAction;
import ca.phon.app.session.editor.view.timeline.RecordGrid.GhostMarker;
import ca.phon.app.session.editor.view.timeline.TimelineRecordTier.SplitMarker;
import ca.phon.app.session.editor.view.timeline.actions.SplitRecordAction;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.Tuple;

public class TimelineRecordTier extends TimelineTier {
	
	private RecordGrid recordGrid;
	
	private Map<Participant, Boolean> speakerVisibility = new HashMap<>();
	
	private Map<String, Boolean> tierVisibility = new HashMap<>();
	
	private TimeUIModel.Interval currentRecordInterval = null;
	
	private int splitGroupIdx = -1;
	
	private SplitMarker splitMarker = null;
	
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
	
	public Interval currentRecordInterval() {
		return this.currentRecordInterval;
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
		
		for(int i = 0; i < 10; i++) {
			final PhonUIAction chSpeakerAct = new PhonUIAction(this, "onChangeSpeakerByIndex", i);
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
			chSpeakerAct.putValue(PhonUIAction.ACCELERATOR_KEY, ks);
			String id = "change_speaker_" + i;

			actionMap.put(id, chSpeakerAct);
			inputMap.put(ks, id);
		}
		
		// split record
		final String splitRecordId = "split_record";
		final SplitRecordAction splitRecordAct = new SplitRecordAction(getParentView());
		final KeyStroke splitRecordKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
		inputMap.put(splitRecordKs, splitRecordId);
		actionMap.put(splitRecordId, splitRecordAct);
		
		final String endSplitId = "end_split_record";
		final PhonUIAction endSplitRecordAct = new PhonUIAction(this, "onEndSplitRecord", false);
		final KeyStroke endSplitRecordKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		inputMap.put(endSplitRecordKs, endSplitId);
		actionMap.put(endSplitId, endSplitRecordAct);
		
		final String acceptSplitId = "accept_split_record";
		final PhonUIAction acceptSplitRecordAct = new PhonUIAction(this, "onEndSplitRecord", true);
		final KeyStroke acceptSplitRecordKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		inputMap.put(acceptSplitRecordKs, acceptSplitId);
		actionMap.put(acceptSplitId, acceptSplitRecordAct);
		
		// modify record split
		final String splitAtGroupId = "split_record_at_group_";
		for(int i = 0; i < 10; i++) {
			final PhonUIAction splitRecordAtGrpAct = new PhonUIAction(this, "onSplitRecordOnGroup", i);
			final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0);
			inputMap.put(ks, splitAtGroupId + i);
			actionMap.put(splitAtGroupId + i, splitRecordAtGrpAct);
		}
		
		recordGrid.setInputMap(WHEN_FOCUSED, inputMap);
		recordGrid.setActionMap(actionMap);
	}
	
	public void onChangeSpeakerByIndex(Integer speakerIdx) {
		if(speakerIdx != 0 && (speakerIdx - 1) >= recordGrid.getSpeakers().size()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		var speaker = (speakerIdx == 0 ? Participant.UNKNOWN : recordGrid.getSpeakers().get(speakerIdx-1));
		
		final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(getParentView().getEditor(), getParentView().getEditor().currentRecord(), speaker);
		getParentView().getEditor().getUndoSupport().postEdit(edit);
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
		if(recordGrid.isSplitMode()) {
			endSplitMode(false);
		}
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
	
	public void beginSplitMode() {
		if(this.splitMarker != null) {
			endSplitMode(false);
		}
		
		// reset split group idx
		splitGroupIdx = -1;
		
		final TimelineView timelineView = getParentView();
		final TimeUIModel timeModel = timelineView.getTimeModel();
		
		final Record record = timelineView.getEditor().currentRecord();
		if(record == null) return;
		
		final MediaSegment segment = record.getSegment().getGroup(0);
		if(segment == null) return;
		
		float segLength = segment.getEndValue() - segment.getStartValue();
		if(segLength <= 0.0f) return;
		
		float middleOfRecord = (float)TimeUIModel.roundTime((segment.getEndValue() - (segLength / 2.0f)) / 1000.0f);
		SplitMarker splitMarker = new SplitMarker(currentRecordInterval(), middleOfRecord);
		
		splitMarker.addPropertyChangeListener("time", (e) -> {
			updateSplitRecords();
		});
		
		timeModel.addMarker(splitMarker);
		
		setSplitMarker(splitMarker);
		var splitRecords = getRecordSplit(middleOfRecord);
		
		getRecordGrid().beginSplitMode(splitRecords.getObj1(), splitRecords.getObj2());
		getRecordGrid().getUI().repaintInterval(currentRecordInterval);
	}
	
	public void endSplitMode(boolean acceptSplit) {
		if(splitMarker != null) {
			getTimeModel().removeMarker(splitMarker);
			this.splitMarker = null;
		}
		
		getRecordGrid().setSplitMode(false);
		if(acceptSplit
				&& getRecordGrid().getLeftRecordSplit() != null
				&& getRecordGrid().getRightRecordSplit() != null) {
			getParentView().getEditor().getUndoSupport().beginUpdate();
			
			int recordIdx = getParentView().getEditor().getCurrentRecordIndex();
			
			DeleteRecordEdit delRecord = new DeleteRecordEdit(getParentView().getEditor());
			getParentView().getEditor().getUndoSupport().postEdit(delRecord);
			
			AddRecordEdit rightRecordEdit = new AddRecordEdit(getParentView().getEditor(),
					getRecordGrid().getRightRecordSplit(), recordIdx);
			getParentView().getEditor().getUndoSupport().postEdit(rightRecordEdit);
			
			AddRecordEdit leftRecordEdit = new AddRecordEdit(getParentView().getEditor(),
					getRecordGrid().getLeftRecordSplit(), recordIdx);
			getParentView().getEditor().getUndoSupport().postEdit(leftRecordEdit);
			
			getParentView().getEditor().getUndoSupport().endUpdate();
		}
		if(currentRecordInterval != null)
			getRecordGrid().getUI().repaintInterval(currentRecordInterval);
	}
	
	public void onEndSplitRecord(PhonActionEvent pae) {
		endSplitMode((boolean)pae.getData());
	}
	
	public void onSplitRecordOnGroup(PhonActionEvent pae) {
		this.splitGroupIdx = (Integer)pae.getData();
		updateSplitRecords();
	}
	
	private void updateSplitRecords() {
		if(splitMarker == null) return;
		
		var recordSplit = getRecordSplit(splitMarker.getTime());
		getRecordGrid().setLeftRecordSplit(recordSplit.getObj1());
		getRecordGrid().setRightRecordSplit(recordSplit.getObj2());
		
		getRecordGrid().getUI().repaintInterval(currentRecordInterval);
	}
	
	private Tuple<Record, Record> getRecordSplit(float splitTime) {
		SessionFactory factory = SessionFactory.newFactory();
		
		Record recordToSplit = getParentView().getEditor().currentRecord();
		MediaSegment seg = recordToSplit.getSegment().getGroup(0);
				
		Record leftRecord = factory.cloneRecord(recordToSplit);
		leftRecord.getSegment().getGroup(0).setEndValue(splitTime * 1000.0f);
		
		Record rightRecord = factory.createRecord();
		rightRecord.addGroup();
		MediaSegment rightSeg = factory.createMediaSegment();
		rightSeg.setStartValue((splitTime * 1000.0f) + 1);
		rightSeg.setEndValue(seg.getEndValue());
		rightRecord.getSegment().setGroup(0, rightSeg);
		
		for(String tierName:leftRecord.getExtraTierNames()) {
			Tier<?> tier = leftRecord.getTier(tierName);
			rightRecord.putTier(factory.createTier(tierName, tier.getDeclaredType(), tier.isGrouped()));
		}
		
		if(splitGroupIdx >= 0) {
			// special case - reverse records
			if(splitGroupIdx == 0) {
				Record t = leftRecord;
				leftRecord = rightRecord;
				rightRecord = t;
				
				MediaSegment ls = leftRecord.getSegment().getGroup(0);
				MediaSegment rs = rightRecord.getSegment().getGroup(0);
				
				leftRecord.getSegment().setGroup(0, rs);
				rightRecord.getSegment().setGroup(0, ls);
			} else if(splitGroupIdx <= leftRecord.numberOfGroups()) {
				MediaSegment ls = leftRecord.getSegment().getGroup(0);
				MediaSegment rs = rightRecord.getSegment().getGroup(0);
				
				rightRecord = factory.cloneRecord(recordToSplit);
				
				for(int i = leftRecord.numberOfGroups() - 1; i >= splitGroupIdx; i--) {
					leftRecord.removeGroup(i);
				}
				
				for(int i = splitGroupIdx-1; i >= 0; i--) {
					rightRecord.removeGroup(i);
				}
				
				leftRecord.getSegment().setGroup(0, ls);
				rightRecord.getSegment().setGroup(0, rs);
			}
		}
		
		leftRecord.setSpeaker(recordToSplit.getSpeaker());
		rightRecord.setSpeaker(recordToSplit.getSpeaker());
		
		return new Tuple<>(leftRecord, rightRecord);
	}
	
	public SplitMarker getSplitMarker() {
		return this.splitMarker;
	}
	
	public void setSplitMarker(SplitMarker splitMarker) {
		this.splitMarker = splitMarker;
	}
	
	@Override
	public void setupContextMenu(MouseEvent me, MenuBuilder builder) {
		setupSplitModeMenu(me, builder);

		var delAction = new DeleteRecordAction(getParentView().getEditor());
		if(me != null)
			delAction.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		builder.addItem(".", new DeleteRecordAction(getParentView().getEditor()));
		
		// change speaker menu
		JMenu changeSpeakerMenu = builder.addMenu(".", "Change participant");
		int speakerNum = 1;
		for(Participant speaker:recordGrid.getSpeakers()) {
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_0 + speakerNum, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
			if(speaker == Participant.UNKNOWN) {
				ks = KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
				changeSpeakerMenu.addSeparator();
			}
			
			final PhonUIAction onChangeSpeakerByIndexAct = new PhonUIAction(this, "onChangeSpeakerByIndex", speakerNum);
			onChangeSpeakerByIndexAct.putValue(PhonUIAction.NAME, speaker.toString());
			onChangeSpeakerByIndexAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Change record speaker to " + speaker.toString());
			if(me != null)
				onChangeSpeakerByIndexAct.putValue(PhonUIAction.ACCELERATOR_KEY, ks);
			if(getParentView().getEditor().currentRecord().getSpeaker() == speaker) {
				onChangeSpeakerByIndexAct.putValue(PhonUIAction.SELECTED_KEY, true);
			} else {
				onChangeSpeakerByIndexAct.putValue(PhonUIAction.SELECTED_KEY, false);
			}
			
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(onChangeSpeakerByIndexAct);
			changeSpeakerMenu.add(menuItem);
			
			++speakerNum;
		}
		
		if(getParentView().getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			builder.addSeparator(".", "play_action");
			
			PlaySegmentAction playAct = new PlaySegmentAction(getParentView().getEditor(), 
					(MediaPlayerEditorView)getParentView().getEditor().getViewModel().getView(MediaPlayerEditorView.VIEW_TITLE));
			if(me != null)
				playAct.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
			builder.addItem(".", playAct);
		}
	}
	
	private void setupSplitModeMenu(MouseEvent me, MenuBuilder builder) {
		if(splitMarker != null) {
			final PhonUIAction acceptSplitAct = new PhonUIAction(this, "onEndSplitRecord", true);
			acceptSplitAct.putValue(PhonUIAction.NAME, "Accept record split");
			acceptSplitAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
			if(me != null)
				acceptSplitAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
			builder.addItem(".", acceptSplitAct);
			
			final PhonUIAction endSplitModeAct = new PhonUIAction(this, "onEndSplitRecord", false);
			endSplitModeAct.putValue(PhonUIAction.NAME, "Exit split record");
			endSplitModeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Exit split record mode without accepting split");
			if(me != null)
				endSplitModeAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
			builder.addItem(".", endSplitModeAct);
			
			// split after group actions
			Record r = getParentView().getEditor().currentRecord();
			if(r != null) {
				JMenu splitMenu = builder.addMenu(".", "Split data after group");
				for(int i = 0; i <= r.numberOfGroups(); i++) {
					final PhonUIAction splitAfterGroupAct = new PhonUIAction(this, "onSplitRecordOnGroup", i);
					if(i == 0) {
						splitAfterGroupAct.putValue(PhonUIAction.NAME, "All data to new record");
					} else {
						splitAfterGroupAct.putValue(PhonUIAction.NAME, "Group " + i);
					}
					if(me != null)
						splitAfterGroupAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0));
					if(splitGroupIdx >= 0) {
						splitAfterGroupAct.putValue(PhonUIAction.SELECTED_KEY, i == splitGroupIdx);
					} else {
						splitAfterGroupAct.putValue(PhonUIAction.SELECTED_KEY, i >= r.numberOfGroups());
					}
					splitMenu.add(new JCheckBoxMenuItem(splitAfterGroupAct));
				}
			}
			
			builder.addSeparator(".", "split_actions");
		} else {
			final PhonUIAction enterSplitModeAct = new PhonUIAction(this, "beginSplitMode");
			enterSplitModeAct.putValue(PhonUIAction.NAME, "Split record");
			enterSplitModeAct.putValue(PhonUIAction.SHORT_DESCRIPTION,  "Enter split record mode for current record");
			if(me != null)
				enterSplitModeAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
			builder.addItem(".", enterSplitModeAct);
		}
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
				// exit split mode if active
				endSplitMode(false);
				
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
			Record r = getParentView().getEditor().getSession().getRecord(recordIndex);
			MediaSegment seg = r.getSegment().getGroup(0);
			mouseDragOffset = getTimeModel().timeAtX(me.getX()) -
					seg.getStartValue() / 1000.0f;
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
				}

				// scroll to mouse position if outside of visible rect
				Rectangle visibleRect = recordGrid.getVisibleRect();
				if(me.getX() < visibleRect.getX()) {
					visibleRect.translate(-10, 0);
					getParentView().scrollRectToVisible(visibleRect);
				} else if(me.getX() > visibleRect.getMaxX()) {
					visibleRect.translate(10, 0);
					getParentView().scrollRectToVisible(visibleRect);
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
	
	/**
	 * The SpligMarker is used when splitting the
	 * current record 
	 *
	 */
	public static class SplitMarker extends TimeUIModel.Marker {

		private Interval parentInterval;
		
		public SplitMarker(Interval parentInterval, float startTime) {
			super(startTime);
			setColor(Color.black);
			setDraggable(true);
			
			this.parentInterval = parentInterval;
		}

		@Override
		public void setTime(float time) {
			if(time <= parentInterval.getStartMarker().getTime()
					|| time >= parentInterval.getEndMarker().getTime())
				return;
			super.setTime(time);
		}
		
	}
	
}
