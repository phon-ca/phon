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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.RecordsTransferable;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.media_player.*;
import ca.phon.app.session.editor.view.session_information.*;
import ca.phon.app.session.editor.view.session_information.actions.*;
import ca.phon.app.session.editor.view.timeline.RecordGrid.*;
import ca.phon.app.session.editor.view.timeline.actions.*;
import ca.phon.media.*;
import ca.phon.media.TimeUIModel.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.menu.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

public class TimelineRecordTier extends TimelineTier implements ClipboardOwner {

	private RecordGrid recordGrid;

	private Map<Participant, Boolean> speakerVisibility = new HashMap<>();

	private Map<String, Boolean> tierVisibility = new HashMap<>();

	private TimeUIModel.Interval currentRecordInterval = null;

	private int splitGroupIdx = -1;

	private SplitMarker splitMarker = null;
	
	private JButton splitButton;
	private JButton cancelSplitButton;
	private JButton acceptSplitButton;

	public TimelineRecordTier(TimelineView parent) {
		super(parent);

		init();
		addToolbarButtons();
		setupEditorEvents();
	}

	private void init() {
		Session session = getParentView().getEditor().getSession();
		recordGrid = new RecordGrid(getTimeModel(), session);
		if (getParentView().getEditor().currentRecord() != null)
			setupRecord(getParentView().getEditor().currentRecord());
		recordGrid.addParticipantMenuHandler(this::setupSpeakerContextMenu);

		recordGrid.addPropertyChangeListener("splitMode", e -> {
			if (!((boolean) e.getNewValue())) {
				endSplitMode(recordGrid.isSplitModeAccept());
			}
		});
		// add listener for changes in selected record which occur from the record grid
		recordGrid.addPropertyChangeListener("currentRecordIndex", e -> {
			int newIdx = (int) e.getNewValue();
			if(newIdx != getParentView().getEditor().getCurrentRecordIndex()) {
				getParentView().getEditor().setCurrentRecordIndex(newIdx);
			}
		});
		recordGrid.addFocusListener(selectionFocusListener);

		recordGrid.setFont(FontPreferences.getTierFont());
		setupRecordGridActions();
		setupSpeakers();

		// add ortho by default
		tierVisibility.put(SystemTierType.Orthography.getName(), Boolean.TRUE);
		tierVisibility.put(SystemTierType.Segment.getName(), Boolean.TRUE);
		setupTiers();

		recordGrid.addRecordGridMouseListener(mouseListener);

		setLayout(new BorderLayout());
		add(recordGrid, BorderLayout.CENTER);
	}

	private void addToolbarButtons() {
		JToolBar toolbar = getParentView().getToolbar();
		
		toolbar.addSeparator();
		
		SplitRecordAction splitAct = new SplitRecordAction(getParentView());
		splitButton = new JButton(splitAct);
		toolbar.add(splitButton);
		splitButton.addActionListener( (e) -> {
			splitButton.setVisible(false);
			cancelSplitButton.setVisible(true);
			acceptSplitButton.setVisible(true);
		});
		
		final PhonUIAction endSplitModeAct = new PhonUIAction(this, "onEndSplitRecord", false);
		endSplitModeAct.putValue(PhonUIAction.NAME, "Exit split record");
		endSplitModeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Exit split record mode without accepting split");
		endSplitModeAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL));
		cancelSplitButton = new JButton(endSplitModeAct);
		cancelSplitButton.setVisible(false);
		toolbar.add(cancelSplitButton);
		
		final PhonUIAction acceptSplitAct = new PhonUIAction(this, "onEndSplitRecord", true);
		acceptSplitAct.putValue(PhonUIAction.NAME, "Accept record split");
		acceptSplitAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
		acceptSplitAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));
		acceptSplitButton = new JButton(acceptSplitAct);
		acceptSplitButton.setVisible(false);
		toolbar.add(acceptSplitButton);
	}

	public RecordGrid getRecordGrid() {
		return this.recordGrid;
	}

	public ListSelectionModel getSelectionModel() {
		return recordGrid.getSelectionModel();
	}

	public Interval currentRecordInterval() {
		return this.currentRecordInterval;
	}

	private void setupRecordGridActions() {
		final InputMap inputMap = recordGrid.getInputMap();
		final ActionMap actionMap = recordGrid.getActionMap();

		final String selectAllKey = "select_all";
		final PhonUIAction selectAllAct = new PhonUIAction(this, "onSelectAll");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), selectAllKey);
		actionMap.put(selectAllKey, selectAllAct);

		final String escapeKey = "escape";
		final PhonUIAction escapeAction = new PhonUIAction(this, "onEscape", false);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escapeKey);
		actionMap.put(escapeKey, escapeAction);

		final String deleteRecordKey = "delete_record";
		final DeleteRecordsAction deleteRecordAction = new DeleteRecordsAction(getParentView());
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), deleteRecordKey);
		actionMap.put(deleteRecordKey, deleteRecordAction);

		final String playSegmentKey = "play_segment";
		final PlaySegmentAction playSegmentAction = new PlaySegmentAction(getParentView().getEditor());
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), playSegmentKey);
		actionMap.put(playSegmentKey, playSegmentAction);

		final String moveRight = "move_segments_right";
		final PhonUIAction moveRightAct = new PhonUIAction(this, "onMoveSegmentsRight", 5);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.CTRL_DOWN_MASK), moveRight);
		actionMap.put(moveRight, moveRightAct);

		final String moveRightSlow = "move_segments_right_slow";
		final PhonUIAction moveRightSlowAct = new PhonUIAction(this, "onMoveSegmentsRight", 1);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), moveRightSlow);
		actionMap.put(moveRightSlow, moveRightSlowAct);

		final String moveLeft = "move_segments_left";
		final PhonUIAction moveLeftAct = new PhonUIAction(this, "onMoveSegmentsLeft", 5);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_DOWN_MASK), moveLeft);
		actionMap.put(moveLeft, moveLeftAct);

		final String moveLeftSlow = "move_segments_left_slow";
		final PhonUIAction moveLeftSlowAct = new PhonUIAction(this, "onMoveSegmentsLeft", 1);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), moveLeftSlow);
		actionMap.put(moveLeftSlow, moveLeftSlowAct);

		final String growSegments = "grow_segments";
		final PhonUIAction growSegmentsAct = new PhonUIAction(this, "onGrowSegments", 3);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK ), growSegments);
		actionMap.put(growSegments, growSegmentsAct);

		final String growSegmentsSlow = "grow_segments_slow";
		final PhonUIAction growSegmentsSlowAct = new PhonUIAction(this, "onGrowSegments", 1);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), growSegmentsSlow);
		actionMap.put(growSegmentsSlow, growSegmentsSlowAct);

		final String shrinkSegments = "shrink_segments";
		final PhonUIAction shrinkSegmentsAct = new PhonUIAction(this, "onShrinkSegments", 3);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK ), shrinkSegments);
		actionMap.put(shrinkSegments, shrinkSegmentsAct);

		final String shrinkSegmentsSlow = "shrink_segments_slow";
		final PhonUIAction shrinkSegmentsSlowAct = new PhonUIAction(this, "onShrinkSegments", 1);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK |KeyEvent.SHIFT_DOWN_MASK), shrinkSegmentsSlow);
		actionMap.put(shrinkSegmentsSlow, shrinkSegmentsSlowAct);

		final PhonUIAction copyRecordsAct = new PhonUIAction(this, "copy");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "copy");
		actionMap.put("copy", copyRecordsAct);

		final PasteRecordAction pasteRecordsAct = new PasteRecordAction(getParentView().getEditor() );
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "paste");
		actionMap.put("paste", pasteRecordsAct);

		for (int i = 0; i < 10; i++) {
			final PhonUIAction chSpeakerAct = new PhonUIAction(this, "onChangeSpeakerByIndex", i);
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
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

		final String acceptSplitId = "accept_split_record";
		final PhonUIAction acceptSplitRecordAct = new PhonUIAction(this, "onEndSplitRecord", true);
		final KeyStroke acceptSplitRecordKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		inputMap.put(acceptSplitRecordKs, acceptSplitId);
		actionMap.put(acceptSplitId, acceptSplitRecordAct);

		// modify record split
		final String splitAtGroupId = "split_record_at_group_";
		for (int i = 0; i < 10; i++) {
			final PhonUIAction splitRecordAtGrpAct = new PhonUIAction(this, "onSplitRecordOnGroup", i);
			final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0);
			inputMap.put(ks, splitAtGroupId + i);
			actionMap.put(splitAtGroupId + i, splitRecordAtGrpAct);
		}

		recordGrid.setInputMap(WHEN_FOCUSED, inputMap);
		recordGrid.setActionMap(actionMap);
	}

	public void onChangeSpeakerByIndex(Integer speakerIdx) {
		if (speakerIdx != 0 && (speakerIdx - 1) >= recordGrid.getSpeakers().size()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		var speaker = (speakerIdx == 0 ? Participant.UNKNOWN : recordGrid.getSpeakers().get(speakerIdx - 1));

		getParentView().getEditor().getUndoSupport().beginUpdate();
		for(int recordIndex:getSelectionModel().getSelectedIndices()) {
			final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(getParentView().getEditor(),
					getParentView().getEditor().getSession().getRecord(recordIndex), speaker);
			getParentView().getEditor().getUndoSupport().postEdit(edit);
		}
		getParentView().getEditor().getUndoSupport().endUpdate();
	}

	public void onMoveSegmentsRight(PhonActionEvent pae) {
		int amount = Integer.parseInt(pae.getData().toString());
		float secondsPerPixel = getTimeModel().timeAtX(getTimeModel().getTimeInsets().left+1);
		float secondsToAdd = amount * secondsPerPixel;

		getParentView().getEditor().getUndoSupport().beginUpdate();
		for(int recordIdx:getSelectionModel().getSelectedIndices()) {
			Record r = getParentView().getEditor().getSession().getRecord(recordIdx);

			MediaSegment recordSeg = r.getSegment().getGroup(0);
			MediaSegment seg = SessionFactory.newFactory().createMediaSegment();
			float startValue = recordSeg.getStartValue() + (1000.0f * secondsToAdd);
			float endValue = recordSeg.getEndValue() + (1000.0f * secondsToAdd);
			if(endValue/1000.0f <= getTimeModel().getEndTime()) {
				seg.setStartValue(startValue);
				seg.setEndValue(endValue);

				TierEdit<MediaSegment> changeSeg = new TierEdit<>(getParentView().getEditor(), r.getSegment(), 0, seg);
				changeSeg.setFireHardChangeOnUndo(true);
				getParentView().getEditor().getUndoSupport().postEdit(changeSeg);
			}
		}
		getParentView().getEditor().getUndoSupport().endUpdate();
		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	public void onGrowSegments(PhonActionEvent pae) {
		int amount = Integer.parseInt(pae.getData().toString());
		float secondsPerPixel = getTimeModel().timeAtX(getTimeModel().getTimeInsets().left+1);
		float secondsToAdd = amount * secondsPerPixel;

		getParentView().getEditor().getUndoSupport().beginUpdate();
		for(int recordIdx:getSelectionModel().getSelectedIndices()) {
			Record r = getParentView().getEditor().getSession().getRecord(recordIdx);

			MediaSegment recordSeg = r.getSegment().getGroup(0);
			MediaSegment seg = SessionFactory.newFactory().createMediaSegment();
			float startValue =  Math.max(0, recordSeg.getStartValue() - (1000.0f * secondsToAdd));
			float endValue = Math.min(getTimeModel().getEndTime() * 1000.0f, recordSeg.getEndValue() + (1000.0f * secondsToAdd));

			seg.setStartValue(startValue);
			seg.setEndValue(endValue);

			TierEdit<MediaSegment> changeSeg = new TierEdit<>(getParentView().getEditor(), r.getSegment(), 0, seg);
			changeSeg.setFireHardChangeOnUndo(true);
			getParentView().getEditor().getUndoSupport().postEdit(changeSeg);
		}
		getParentView().getEditor().getUndoSupport().endUpdate();
		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	public void onShrinkSegments(PhonActionEvent pae) {
		int amount = Integer.parseInt(pae.getData().toString());
		float secondsPerPixel = getTimeModel().timeAtX(getTimeModel().getTimeInsets().left+1);
		float secondsToSubtract = amount * secondsPerPixel;

		getParentView().getEditor().getUndoSupport().beginUpdate();
		for(int recordIdx:getSelectionModel().getSelectedIndices()) {
			Record r = getParentView().getEditor().getSession().getRecord(recordIdx);

			MediaSegment recordSeg = r.getSegment().getGroup(0);
			MediaSegment seg = SessionFactory.newFactory().createMediaSegment();
			float startValue = recordSeg.getStartValue() + (1000.0f * secondsToSubtract);
			float endValue = recordSeg.getEndValue() - (1000.0f * secondsToSubtract);

			if(startValue <= endValue) {
				seg.setStartValue(startValue);
				seg.setEndValue(endValue);

				TierEdit<MediaSegment> changeSeg = new TierEdit<>(getParentView().getEditor(), r.getSegment(), 0, seg);
				changeSeg.setFireHardChangeOnUndo(true);
				getParentView().getEditor().getUndoSupport().postEdit(changeSeg);
			}
		}
		getParentView().getEditor().getUndoSupport().endUpdate();
		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	public void onMoveSegmentsLeft(PhonActionEvent pae) {
		int amount = Integer.parseInt(pae.getData().toString());
		float secondsPerPixel = getTimeModel().timeAtX(getTimeModel().getTimeInsets().left+1);
		float secondsToAdd = amount * secondsPerPixel;

		getParentView().getEditor().getUndoSupport().beginUpdate();
		for(int recordIdx:getSelectionModel().getSelectedIndices()) {
			Record r = getParentView().getEditor().getSession().getRecord(recordIdx);

			MediaSegment recordSeg = r.getSegment().getGroup(0);
			MediaSegment seg = SessionFactory.newFactory().createMediaSegment();
			float startValue =  recordSeg.getStartValue() - (1000.0f * secondsToAdd);
			float endValue = recordSeg.getEndValue() - (1000.0f * secondsToAdd);

			if(startValue >= 0) {
				seg.setStartValue(startValue);
				seg.setEndValue(endValue);

				TierEdit<MediaSegment> changeSeg = new TierEdit<>(getParentView().getEditor(), r.getSegment(), 0, seg);
				changeSeg.setFireHardChangeOnUndo(true);
				getParentView().getEditor().getUndoSupport().postEdit(changeSeg);
			}
		}
		getParentView().getEditor().getUndoSupport().endUpdate();
		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	public void onEscape(PhonActionEvent pae) {
		if (isSplitModeActive()) {
			onEndSplitRecord(pae);
		} else if (getParentView().getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_TITLE)) {
			MediaPlayerEditorView mediaView = (MediaPlayerEditorView) getParentView().getEditor().getViewModel()
					.getView(MediaPlayerEditorView.VIEW_TITLE);
			if (mediaView.getPlayer().isPlaying()) {
				mediaView.getPlayer().pause();
				return;
			}
		}
		if(getSelectionModel().getSelectedItemsCount() > 1) {
			// reset record selection
			getSelectionModel().setSelectionInterval(getParentView().getEditor().getCurrentRecordIndex(),
					getParentView().getEditor().getCurrentRecordIndex());
			recordGrid.repaint(recordGrid.getVisibleRect());
		}
	}

	public void onSelectSpeaker(PhonActionEvent pae) {
		Participant speaker = (Participant) pae.getData();
		if(speaker == null) return;

		for(int rIdx = 0; rIdx < getParentView().getEditor().getSession().getRecordCount(); rIdx++) {
			Record r = getParentView().getEditor().getSession().getRecord(rIdx);
			if(r.getSpeaker() == speaker) {
				getSelectionModel().addSelectionInterval(rIdx, rIdx);
			}
		}
	}

	public void onSelectAll(PhonActionEvent pae) {
		List<Integer> visibleRecords = new ArrayList<>();
		List<Participant> visibleSpeakers = getSpeakerList();
		if(isSpeakerVisible(Participant.UNKNOWN))
			visibleSpeakers.add(Participant.UNKNOWN);
		for(int i = 0; i < getParentView().getEditor().getSession().getRecordCount(); i++) {
			if(visibleSpeakers.contains(getParentView().getEditor().getSession().getRecord(i).getSpeaker())) {
				visibleRecords.add(i);
			}
		}
		if(visibleRecords.size() == 0) return;

		if(getSelectionModel().getSelectedItemsCount() == visibleRecords.size()) {
			getSelectionModel().setSelectionInterval(getParentView().getEditor().getCurrentRecordIndex(),
					getParentView().getEditor().getCurrentRecordIndex());
		} else {
			for(int visibleRecord:visibleRecords) {
				getSelectionModel().addSelectionInterval(visibleRecord, visibleRecord);
			}
		}
		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	private final DelegateEditorAction onRecordChange = new DelegateEditorAction(this, "onRecordChange");

	private final DelegateEditorAction onSpeakerChange = new DelegateEditorAction(this, "onSpeakerChange");

	private final DelegateEditorAction onRecordDeleted = new DelegateEditorAction(this, "onRecordDeleted");

	private final DelegateEditorAction onTierChangedAct = new DelegateEditorAction(this, "onTierChanged");

	private final DelegateEditorAction onParticipantRemoveAct = new DelegateEditorAction(this, "onParticipantRemoved");

	private final DelegateEditorAction onParticipantAddedAct = new DelegateEditorAction(this, "onParticipantAdded");

	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT,
				onRecordChange);
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.SPEAKER_CHANGE_EVT,
				onSpeakerChange);
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGED_EVT,
				onTierChangedAct);
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED,
				onParticipantRemoveAct);
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_ADDED,
				onParticipantAddedAct);
		getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT,
				onRecordDeleted);
	}

	private void deregisterEditorEvents() {
		getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT,
				onRecordChange);
		getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.SPEAKER_CHANGE_EVT,
				onSpeakerChange);
		getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.TIER_CHANGED_EVT,
				onTierChangedAct);
		getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.PARTICIPANT_REMOVED,
				onParticipantRemoveAct);
		getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.PARTICIPANT_ADDED,
				onParticipantAddedAct);
		getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_DELETED_EVT,
				onRecordDeleted);
	}

	private void updateCurrentRecordInterval(Record r) {
		if(r == null) return;

		if(currentRecordInterval != null)
			getTimeModel().removeInterval(currentRecordInterval);

		int rIdx = getParentView().getEditor().getCurrentRecordIndex();

		MediaSegment segment = r.getSegment().getGroup(0);
		var segStartTime = segment.getStartValue() / 1000.0f;
		var segEndTime = segment.getEndValue() / 1000.0f;

		// check for 'GhostMarker's which, if present, will
		// become the start/end marker for the record interval
		Optional<RecordGrid.GhostMarker> ghostMarker = recordGrid.getTimeModel().getMarkers().parallelStream()
				.filter((m) -> m instanceof GhostMarker).map((m) -> GhostMarker.class.cast(m)).findAny();
		if (ghostMarker.isPresent() && recordGrid.getUI().getCurrentlyDraggedMarker() == ghostMarker.get()) {
			Marker startMarker = ghostMarker.get().isStart() ? ghostMarker.get() : new Marker(segStartTime);
			Marker endMarker = ghostMarker.get().isStart() ? new Marker(segEndTime) : ghostMarker.get();
			currentRecordInterval = getTimeModel().addInterval(startMarker, endMarker);
			currentRecordInterval.setRepaintEntireInterval(true);
			currentRecordInterval.addPropertyChangeListener(new RecordIntervalListener());
			recordGrid.getUI().beginDrag(currentRecordInterval, ghostMarker.get());
		} else {
			currentRecordInterval = getTimeModel().addInterval(segStartTime, segEndTime);
			currentRecordInterval.setRepaintEntireInterval(true);
			currentRecordInterval.addPropertyChangeListener(new RecordIntervalListener());
		}
		currentRecordInterval.getStartMarker()
				.setColor(UIManager.getColor(recordGrid.hasFocus() ? TimelineViewColors.FOCUSED_INTERVAL_MARKER_COLOR
						: TimelineViewColors.INTERVAL_MARKER_COLOR));
		currentRecordInterval.getEndMarker()
				.setColor(UIManager.getColor(recordGrid.hasFocus() ? TimelineViewColors.FOCUSED_INTERVAL_MARKER_COLOR
						: TimelineViewColors.INTERVAL_MARKER_COLOR));
		currentRecordInterval
				.setColor(UIManager.getColor(recordGrid.hasFocus() ? TimelineViewColors.FOCUSED_INTERVAL_BACKGROUND
						: TimelineViewColors.INTERVAL_BACKGROUND));
	}

	public void setupRecord(Record r) {
		updateCurrentRecordInterval(r);

		if(r != null) {
			recordGrid.setCurrentRecord(r);
		} else {
			getSelectionModel().clearSelection();
		}
		
		mouseListener.waitForRecordChange = false;
	}

	/* Editor events */
	@RunOnEDT
	public void onRecordChange(EditorEvent evt) {
		Record r = (Record) evt.getEventData();
		// don't update if already switched to record
		if(recordGrid.getCurrentRecordIndex() == getParentView().getEditor().getCurrentRecordIndex()) {
			updateCurrentRecordInterval(r);
			return;
		}

		if (recordGrid.isSplitMode()) {
			recordGrid.setSplitModeAccept(false);
			recordGrid.setSplitMode(false);
		}
		setupRecord(r);

		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	@RunOnEDT
	public void onRecordDeleted(EditorEvent evt) {
		if (currentRecordInterval != null) {
			getTimeModel().removeInterval(currentRecordInterval);

			Record currentRecord = getParentView().getEditor().currentRecord();
			if (currentRecord != null) {
				setupRecord(currentRecord);
			}
		}
	}

	@RunOnEDT
	public void onSpeakerChange(EditorEvent evt) {
		recordGrid.repaint(recordGrid.getVisibleRect());
	}

	@RunOnEDT
	public void onParticipantRemoved(EditorEvent ee) {
		speakerVisibility.remove((Participant) ee.getEventData());
		setupSpeakers();
	}

	@RunOnEDT
	public void onParticipantAdded(EditorEvent ee) {
		setupSpeakers();
	}

	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if (SystemTierType.Orthography.getName().equals(ee.getEventData().toString())
				|| SystemTierType.Segment.getName().equals(ee.getEventData().toString())) {
			updateCurrentRecordInterval(getParentView().getEditor().currentRecord());
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

		if (speakerVisibility.containsKey(speaker))
			retVal = speakerVisibility.get(speaker);

		return retVal;
	}

	public void setSpeakerVisible(Participant speaker, boolean visible) {
		speakerVisibility.put(speaker, visible);
		setupSpeakers();
	}

	public void toggleSpeaker(PhonActionEvent pae) {
		Participant speaker = (Participant) pae.getData();
		setSpeakerVisible(speaker, !isSpeakerVisible(speaker));
	}

	public List<Participant> getSpeakerList() {
		List<Participant> retVal = new ArrayList<>();

		Session session = getParentView().getEditor().getSession();
		for (var speaker : session.getParticipants()) {
			if (isSpeakerVisible(speaker)) {
				retVal.add(speaker);
			}
		}

		return retVal;
	}

	private void setupSpeakers() {
		Session session = getParentView().getEditor().getSession();

		var speakerList = new ArrayList<Participant>();
		for (var speaker : session.getParticipants()) {
			if (isSpeakerVisible(speaker))
				speakerList.add(speaker);
		}
		if (isSpeakerVisible(Participant.UNKNOWN))
			speakerList.add(Participant.UNKNOWN);
		recordGrid.setSpeakers(speakerList);
	}

	public boolean isTierVisible(String tierName) {
		boolean retVal = false;

		if (tierVisibility.containsKey(tierName))
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
		recordGrid.setTiers(session.getTierView().stream().map(TierViewItem::getTierName).filter(this::isTierVisible)
				.collect(Collectors.toList()));
	}

	public boolean isSplitModeActive() {
		return this.splitMarker != null;
	}

	public void beginSplitMode() {
		if (this.splitMarker != null) {
			endSplitMode(false);
		}

		// reset split group idx
		splitGroupIdx = -1;

		final TimelineView timelineView = getParentView();
		final TimeUIModel timeModel = timelineView.getTimeModel();

		final Record record = timelineView.getEditor().currentRecord();
		if (record == null)
			return;

		final MediaSegment segment = record.getSegment().getGroup(0);
		if (segment == null)
			return;

		float segLength = segment.getEndValue() - segment.getStartValue();
		if (segLength <= 0.0f)
			return;

		float middleOfRecord = (float) TimeUIModel.roundTime((segment.getEndValue() - (segLength / 2.0f)) / 1000.0f);
		SplitMarker splitMarker = new SplitMarker(currentRecordInterval(), middleOfRecord);

		splitMarker.addPropertyChangeListener("time", (e) -> {
			updateSplitRecordTimes((float) e.getNewValue());
		});

		timeModel.addMarker(splitMarker);

		setSplitMarker(splitMarker);
		var splitRecords = getRecordSplit(middleOfRecord);

		getRecordGrid().beginSplitMode(splitRecords.getObj1(), splitRecords.getObj2());
		getRecordGrid().repaintInterval(currentRecordInterval);
		
		splitButton.setVisible(false);
		acceptSplitButton.setVisible(true);
		cancelSplitButton.setVisible(true);
	}

	private void endSplitMode(boolean acceptSplit) {
		if (splitMarker != null) {
			getTimeModel().removeMarker(splitMarker);
			this.splitMarker = null;
		}

		// getRecordGrid().setSplitMode(false);
		if (acceptSplit && getRecordGrid().getLeftRecordSplit() != null
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
		if (currentRecordInterval != null)
			getRecordGrid().repaintInterval(currentRecordInterval);
		
		splitButton.setVisible(true);
		cancelSplitButton.setVisible(false);
		acceptSplitButton.setVisible(false);
	}

	public void onEndSplitRecord(PhonActionEvent pae) {
		recordGrid.setSplitModeAccept((boolean) pae.getData());
		recordGrid.setSplitMode(false);
	}

	public void onSplitRecordOnGroup(PhonActionEvent pae) {
		this.splitGroupIdx = (Integer) pae.getData();
		updateSplitRecords();
	}

	private void updateSplitRecords() {
		if (splitMarker == null)
			return;

		var recordSplit = getRecordSplit(splitMarker.getTime());
		getRecordGrid().setLeftRecordSplit(recordSplit.getObj1());
		getRecordGrid().setRightRecordSplit(recordSplit.getObj2());

		getRecordGrid().repaintInterval(currentRecordInterval);
	}

	private void updateSplitRecordTimes(float splitTime) {
		Record leftRecord = recordGrid.getLeftRecordSplit();
		MediaSegment leftSeg = leftRecord.getSegment().getGroup(0);
		leftSeg.setEndValue(splitTime * 1000.0f);

		Record rightRecord = recordGrid.getRightRecordSplit();
		MediaSegment rightSeg = rightRecord.getSegment().getGroup(0);

		long segOffset = (long) Math.ceil(timeAtX(getTimeModel().getTimeInsets().left + 1) * 1000.0f);
		rightSeg.setStartValue((splitTime * 1000.0f) + segOffset);

		getRecordGrid().repaintInterval(currentRecordInterval);
	}

	private Tuple<Record, Record> getRecordSplit(float splitTime) {
		final SessionFactory sessionFactory = SessionFactory.newFactory();

		Record recordToSplit = getParentView().getEditor().currentRecord();
		MediaSegment seg = recordToSplit.getSegment().getGroup(0);

		Record leftRecord = sessionFactory.cloneRecord(recordToSplit);
		leftRecord.getSegment().getGroup(0).setEndValue(splitTime * 1000.0f);

		Record rightRecord = sessionFactory.createRecord();
		rightRecord.addGroup();
		MediaSegment rightSeg = sessionFactory.createMediaSegment();
		long segOffset = (long) Math.ceil(timeAtX(getTimeModel().getTimeInsets().left + 1) * 1000.0f);
		rightSeg.setStartValue((splitTime * 1000.0f) + segOffset);
		rightSeg.setEndValue(seg.getEndValue());
		rightRecord.getSegment().setGroup(0, rightSeg);

		for (String tierName : leftRecord.getExtraTierNames()) {
			Tier<?> tier = leftRecord.getTier(tierName);
			rightRecord.putTier(sessionFactory.createTier(tierName, tier.getDeclaredType(), tier.isGrouped()));
		}

		if (splitGroupIdx >= 0) {
			// special case - reverse records
			if (splitGroupIdx == 0) {
				Record t = leftRecord;
				leftRecord = rightRecord;
				rightRecord = t;

				MediaSegment ls = leftRecord.getSegment().getGroup(0);
				MediaSegment rs = rightRecord.getSegment().getGroup(0);

				leftRecord.getSegment().setGroup(0, rs);
				rightRecord.getSegment().setGroup(0, ls);
			} else if (splitGroupIdx <= leftRecord.numberOfGroups()) {
				MediaSegment ls = leftRecord.getSegment().getGroup(0);
				MediaSegment rs = rightRecord.getSegment().getGroup(0);

				rightRecord = sessionFactory.cloneRecord(recordToSplit);

				for (int i = leftRecord.numberOfGroups() - 1; i >= splitGroupIdx; i--) {
					leftRecord.removeGroup(i);
				}

				for (int i = splitGroupIdx - 1; i >= 0; i--) {
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
	public void setupContextMenu(MenuBuilder builder, boolean includeAccel) {
		final PhonUIAction selectAllAct = new PhonUIAction(this, "onSelectAll");
		selectAllAct.putValue(PhonUIAction.NAME, "Select all");
		selectAllAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select all visible records");
		if(includeAccel)
			selectAllAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		builder.addItem(".", selectAllAct);

		JMenu speakerMenu = builder.addMenu(".", "Select all for participant");
		for(Participant speaker:getSpeakerList()) {
			PhonUIAction selectSpeakerAct = new PhonUIAction(this, "onSelectSpeaker", speaker);
			selectSpeakerAct.putValue(PhonUIAction.NAME, speaker.toString());
			speakerMenu.add(selectSpeakerAct);
		}
		if(isSpeakerVisible(Participant.UNKNOWN)) {
			PhonUIAction selectSpeakerAct = new PhonUIAction(this, "onSelectSpeaker", Participant.UNKNOWN);
			selectSpeakerAct.putValue(PhonUIAction.NAME, Participant.UNKNOWN.toString());
			speakerMenu.add(selectSpeakerAct);
		}

		builder.addSeparator(".", "record_seleciton");

		final PhonUIAction moveSegmentsRightAct = new PhonUIAction(this, "onMoveSegmentsRight", 5);
		moveSegmentsRightAct.putValue(PhonUIAction.NAME, "Move record" + (getSelectionModel().getSelectedItemsCount() > 1 ? "s" : "") + " right");
		moveSegmentsRightAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected records right on the timeline");
		if(includeAccel)
			moveSegmentsRightAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.CTRL_DOWN_MASK));
		builder.addItem(".", moveSegmentsRightAct);

		final PhonUIAction moveSegmentsLeftAct = new PhonUIAction(this, "onMoveSegmentsLeft", 5);
		moveSegmentsLeftAct.putValue(PhonUIAction.NAME, "Move record" + (getSelectionModel().getSelectedItemsCount() > 1 ? "s" : "") + " left");
		moveSegmentsLeftAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected records left on the timeline");
		if(includeAccel)
			moveSegmentsLeftAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_DOWN_MASK));
		builder.addItem(".", moveSegmentsLeftAct);

		final PhonUIAction growSegmentsAct = new PhonUIAction(this, "onGrowSegments", 3);
		growSegmentsAct.putValue(PhonUIAction.NAME, "Grow record" + (getSelectionModel().getSelectedItemsCount() > 1 ? "s" : ""));
		growSegmentsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Grow selected records");
		if(includeAccel)
			growSegmentsAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));
		builder.addItem(".", growSegmentsAct);

		final PhonUIAction shrinkSegmentsAct = new PhonUIAction(this, "onShrinkSegments", 3);
		shrinkSegmentsAct.putValue(PhonUIAction.NAME, "Shrink record" + (getSelectionModel().getSelectedItemsCount() > 1 ? "s" : ""));
		shrinkSegmentsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Shrink selected records");
		if(includeAccel)
			shrinkSegmentsAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK));
		builder.addItem(".", shrinkSegmentsAct);

		builder.addSeparator(".", "segment_times");

		setupSplitModeMenu(builder, includeAccel);

		var delAction = new DeleteRecordsAction(getParentView());
		if (includeAccel)
			delAction.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		builder.addItem(".", new DeleteRecordsAction(getParentView()));

		// change speaker menu
		JMenu changeSpeakerMenu = builder.addMenu(".", "Change participant");
		changeSpeakerMenu.setIcon(IconManager.getInstance().getIcon("apps/system-users", IconSize.SMALL));
		int speakerNum = 1;
		for (Participant speaker : recordGrid.getSpeakers()) {
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_0 + speakerNum,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
			if (speaker == Participant.UNKNOWN) {
				ks = KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
				changeSpeakerMenu.addSeparator();
			}

			final PhonUIAction onChangeSpeakerByIndexAct = new PhonUIAction(this, "onChangeSpeakerByIndex", speakerNum);
			onChangeSpeakerByIndexAct.putValue(PhonUIAction.NAME, speaker.toString());
			onChangeSpeakerByIndexAct.putValue(PhonUIAction.SHORT_DESCRIPTION,
					"Change record speaker to " + speaker.toString());
			if (includeAccel)
				onChangeSpeakerByIndexAct.putValue(PhonUIAction.ACCELERATOR_KEY, ks);
			if (getParentView().getEditor().currentRecord() != null
					&& getParentView().getEditor().currentRecord().getSpeaker() == speaker) {
				onChangeSpeakerByIndexAct.putValue(PhonUIAction.SELECTED_KEY, true);
			} else {
				onChangeSpeakerByIndexAct.putValue(PhonUIAction.SELECTED_KEY, false);
			}

			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(onChangeSpeakerByIndexAct);
			changeSpeakerMenu.add(menuItem);

			++speakerNum;
		}
	}

	private void setupSplitModeMenu(MenuBuilder builder, boolean includeAccel) {
		if (splitMarker != null) {
			final PhonUIAction acceptSplitAct = new PhonUIAction(this, "onEndSplitRecord", true);
			acceptSplitAct.putValue(PhonUIAction.NAME, "Accept record split");
			acceptSplitAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
			acceptSplitAct.putValue(PhonUIAction.SMALL_ICON,
					IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL));
			if (includeAccel)
				acceptSplitAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
			builder.addItem(".", acceptSplitAct);

			final PhonUIAction endSplitModeAct = new PhonUIAction(this, "onEndSplitRecord", false);
			endSplitModeAct.putValue(PhonUIAction.NAME, "Exit split record");
			endSplitModeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Exit split record mode without accepting split");
			endSplitModeAct.putValue(PhonUIAction.SMALL_ICON,
					IconManager.getInstance().getIcon("actions/button_cancel", IconSize.SMALL));
			if (includeAccel)
				endSplitModeAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
			builder.addItem(".", endSplitModeAct);

			// split after group actions
			Record r = getParentView().getEditor().currentRecord();
			if (r != null) {
				JMenu splitMenu = builder.addMenu(".", "Split data after group");
				for (int i = 0; i <= r.numberOfGroups(); i++) {
					final PhonUIAction splitAfterGroupAct = new PhonUIAction(this, "onSplitRecordOnGroup", i);
					if (i == 0) {
						splitAfterGroupAct.putValue(PhonUIAction.NAME, "All data to new record");
					} else {
						splitAfterGroupAct.putValue(PhonUIAction.NAME, "Group " + i);
					}
					if (includeAccel)
						splitAfterGroupAct.putValue(PhonUIAction.ACCELERATOR_KEY,
								KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0));
					if (splitGroupIdx >= 0) {
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
			enterSplitModeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Enter split record mode for current record");
			if (includeAccel)
				enterSplitModeAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
			builder.addItem(".", enterSplitModeAct);
		}
	}
	
	public void setupSpeakerContextMenu(Participant participant, MenuBuilder builder) {
		builder.addSeparator(".", "select");
		final PhonUIAction selectAllAct = new PhonUIAction(this, "onSelectSpeaker", participant);
		selectAllAct.putValue(PhonUIAction.NAME, "Select all");
		selectAllAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select all records for " + participant.toString());
		builder.addItem(".", selectAllAct);

		Session session = getParentView().getEditor().getSession();
		JMenu reassignMenu = new JMenu("Reassign records to");
		for(Participant speaker:session.getParticipants()) {
			if(speaker != participant) {
				final ReassignRecordsAction reassignAct = new ReassignRecordsAction(getParentView(), participant, speaker);
				reassignAct.putValue(Action.NAME, speaker.toString());
				reassignAct.putValue(Action.SHORT_DESCRIPTION, String.format("Reassign all records from %s to %s", participant, speaker));
				reassignMenu.add(reassignAct);
			}
		}
		if(Participant.UNKNOWN != participant) {
			final ReassignRecordsAction reassignAct = new ReassignRecordsAction(getParentView(), participant, Participant.UNKNOWN);
			reassignAct.putValue(Action.NAME, Participant.UNKNOWN.toString());
			reassignAct.putValue(Action.SHORT_DESCRIPTION, String.format("Reassign all records from %s to %s", participant, Participant.UNKNOWN));
			reassignMenu.add(reassignAct);
		}
		builder.addSeparator(".", "reassign");
		if(reassignMenu.getItemCount() > 0) {
			builder.addMenu(".", reassignMenu);
		}
	}

	public void setupSpeakerMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for (var speaker : session.getParticipants()) {
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
				(SessionInfoEditorView) getParentView().getEditor().getViewModel()
						.getView(SessionInfoEditorView.VIEW_TITLE));
		builder.addItem(".", newParticipantAct);
	}

	public void setupTierMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for (var tierViewItem : session.getTierView()) {
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

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	private class RecordIntervalListener implements PropertyChangeListener {

		private boolean isFirstChange = true;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Record r = recordGrid.getCurrentRecord();
			if(r == null) return;
			
			MediaSegment segment = r.getSegment().getGroup(0);
			final SessionFactory factory = SessionFactory.newFactory();

			if(evt.getPropertyName().equals("valueAdjusting")) {
				if(recordGrid.isFocusable()) {
					recordGrid.requestFocusInWindow();
				}
				// exit split mode if active
				recordGrid.setSplitModeAccept(false);
				recordGrid.setSplitMode(false);
				
				if((boolean)evt.getNewValue()) {
					isFirstChange = true;
					getParentView().getEditor().getUndoSupport().beginUpdate();
				} else {
					getParentView().getEditor().getUndoSupport().endUpdate();
					getParentView().getEditor().getEventManager().queueEvent(new EditorEvent(EditorEventType.TIER_CHANGED_EVT, TimelineRecordTier.class, SystemTierType.Segment.getName()));
				}
			} else if(evt.getPropertyName().endsWith("time")) {
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
				
				// XXX repaint grid - requried to fix incorrect flags and record numbers
				recordGrid.repaint(recordGrid.getVisibleRect());
			}
		}

	}

	private FocusListener selectionFocusListener = new FocusListener() {

		@Override
		public void focusLost(FocusEvent e) {
			if (currentRecordInterval != null) {
				currentRecordInterval.setColor(UIManager.getColor(TimelineViewColors.INTERVAL_BACKGROUND));
				currentRecordInterval.getStartMarker()
						.setColor(UIManager.getColor(TimelineViewColors.INTERVAL_MARKER_COLOR));
				currentRecordInterval.getEndMarker()
						.setColor(UIManager.getColor(TimelineViewColors.INTERVAL_MARKER_COLOR));
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (currentRecordInterval != null) {
				currentRecordInterval.setColor(UIManager.getColor(TimelineViewColors.FOCUSED_INTERVAL_BACKGROUND));
				currentRecordInterval.getStartMarker()
						.setColor(UIManager.getColor(TimelineViewColors.FOCUSED_INTERVAL_MARKER_COLOR));
				currentRecordInterval.getEndMarker()
						.setColor(UIManager.getColor(TimelineViewColors.FOCUSED_INTERVAL_MARKER_COLOR));
			}
		}
	};
//
//	private Participant cancelParticipant = Participant.UNKNOWN;
//
//	private float cancelStartTime = -1.0f;
//
//	private float cancelEndTime = -1.0f;
//
//	private int currentDraggedRecord = -1;
//
//	private float mouseDragOffset = -1.0f;

	private class DragData {
		/*
		 * Participant being dragged or Participant.ALL if
		 * multiple participants are being dragged
		 */
		Participant participant;

		int draggedRecord = -1;
		float mouseDragOffset = -1.0f;

		Map<Integer, MediaSegment> originalSegments = new LinkedHashMap<>();

		boolean isFirstChange = true;
	}
	private final DragData dragData = new DragData();

	private void beingRecordDrag(int recordIndex) {
		Toolkit.getDefaultToolkit().addAWTEventListener(cancelDragListener, AWTEvent.KEY_EVENT_MASK);
		dragData.draggedRecord = recordIndex;
		Set<Participant> participants = new HashSet<>();

		dragData.originalSegments.clear();
		for(int selectedRecord:getSelectionModel().getSelectedIndices()) {
			Record r = getRecordGrid().getSession().getRecord(selectedRecord);
			participants.add(r.getSpeaker());

			MediaSegment recordSeg = r.getSegment().getGroup(0);
			MediaSegment origSeg = SessionFactory.newFactory().createMediaSegment();
			origSeg.setStartValue(recordSeg.getStartValue());
			origSeg.setEndValue(recordSeg.getEndValue());
			dragData.originalSegments.put(selectedRecord, origSeg);
		}
		dragData.participant = (participants.size() == 1 ? participants.iterator().next() : Participant.ALL);
		currentRecordInterval.setValueAdjusting(true);

		dragData.isFirstChange = true;
	}
	
	private void endRecordDrag() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(cancelDragListener);
		if (currentRecordInterval != null)
			currentRecordInterval.setValueAdjusting(false);
		dragData.draggedRecord = -1;
	}
	
	private void cancelRecordDrag() {
		for(int recordIndex:getSelectionModel().getSelectedIndices()) {
			Record r = getParentView().getEditor().getSession().getRecord(recordIndex);
			if(dragData.participant != Participant.ALL)
				r.setSpeaker(dragData.participant);
			MediaSegment origSegment = dragData.originalSegments.get(recordIndex);
			if (currentRecordInterval != null && getRecordGrid().getCurrentRecordIndex() == recordIndex) {
				currentRecordInterval.getStartMarker().setTime(origSegment.getStartValue());
				currentRecordInterval.getEndMarker().setTime(origSegment.getEndValue());
			} else {
				MediaSegment recordSeg = r.getSegment().getGroup(0);
				recordSeg.setStartValue(origSegment.getStartValue());
				recordSeg.setEndValue(origSegment.getEndValue());
			}
		}
		dragData.mouseDragOffset = -1.0f;
		endRecordDrag();
	}

	/*
	 * Copy & Paste
	 */

	/**
	 * Copy selected records to clipboard.
	 */
	public void copy() {
		RecordsTransferable transferable = new RecordsTransferable(getParentView().getEditor().getSession(),
				getSelectionModel().getSelectedIndices());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, this);
	}

	private final AWTEventListener cancelDragListener = new AWTEventListener() {
		
		@Override
		public void eventDispatched(AWTEvent event) {
			if(event instanceof KeyEvent) {
				KeyEvent ke = (KeyEvent)event;
				if(ke.getID() == KeyEvent.KEY_PRESSED && 
						ke.getKeyChar() == KeyEvent.VK_ESCAPE) {
					cancelRecordDrag();
					((KeyEvent) event).consume();
				}
			}
		}
	};

	private final RecordMouseListener mouseListener = new RecordMouseListener();
	
	private class RecordMouseListener extends RecordGridMouseAdapter {

		// offset (in sec) from left of interval where we are starting the drag
		
		volatile boolean waitForRecordChange = false;
		

		@Override
		public void recordClicked(int recordIndex, MouseEvent me) {
			if(me.getButton() != MouseEvent.BUTTON1) return;

			if((me.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) == Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) {
				if(getSelectionModel().isSelectedIndex(recordIndex))
					getSelectionModel().removeSelectionInterval(recordIndex, recordIndex);
				else
					getSelectionModel().addSelectionInterval(recordIndex, recordIndex);
			} else if(me.getModifiersEx() == 0) {
				getParentView().getEditor().setCurrentRecordIndex(recordIndex);
			}
		}

		@Override
		public void recordPressed(int recordIndex, MouseEvent me) {
			Record r = getParentView().getEditor().getSession().getRecord(recordIndex);
			MediaSegment seg = r.getSegment().getGroup(0);
			dragData.mouseDragOffset = getTimeModel().timeAtX(me.getX()) - seg.getStartValue() / 1000.0f;
		}

		@Override
		public void recordReleased(int recordIndex, MouseEvent me) {
			endRecordDrag();
		}

		@Override
		public void recordDragged(int recordIndex, MouseEvent me) {
			if (!getSelectionModel().isSelectedIndex(recordIndex)) {
				if((me.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
					getSelectionModel().addSelectionInterval(recordIndex, recordIndex);
				} else {
					getParentView().getEditor().setCurrentRecordIndex(recordIndex);
					waitForRecordChange = true;
				}
				return;
			} else if(waitForRecordChange) {
				return;
			} else {
				// shouldn't happen
				if (currentRecordInterval == null)
					return;

				if (dragData.draggedRecord != recordIndex) {
					// don't adjust an already changing interval
					if (currentRecordInterval.isValueAdjusting())
						return;
					
					if(dragData.mouseDragOffset < 0) return;

					beingRecordDrag(recordIndex);
				}

				// scroll to mouse position if outside of visible rect
				Rectangle visibleRect = recordGrid.getVisibleRect();
				if (me.getX() < visibleRect.getX()) {
					visibleRect.translate(-10, 0);
					getParentView().scrollRectToVisible(visibleRect);
				} else if (me.getX() > visibleRect.getMaxX()) {
					visibleRect.translate(10, 0);
					getParentView().scrollRectToVisible(visibleRect);
				}

				Participant mouseOverSpeaker = recordGrid.getUI().getSpeakerAtPoint(me.getPoint());
				if (dragData.participant != Participant.ALL
						&& mouseOverSpeaker != null
						&& mouseOverSpeaker != getParentView().getEditor().currentRecord().getSpeaker()) {
					// change participants if all selections are in the same speaker tier
					for(int rIdx:getSelectionModel().getSelectedIndices()) {
						// change speakers
						ChangeSpeakerEdit chEdit = new ChangeSpeakerEdit(getParentView().getEditor(),
								getParentView().getEditor().getSession().getRecord(rIdx), mouseOverSpeaker);
						getParentView().getEditor().getUndoSupport().postEdit(chEdit);
					}
				}

				Record dragRecord = getRecordGrid().getSession().getRecord(dragData.draggedRecord);
				MediaSegment dragSeg = dragRecord.getSegment().getGroup(0);

				float startTime = dragSeg.getStartValue() / 1000.0f;
				float oldOffsetTime = startTime + dragData.mouseDragOffset;
				float newOffsetTime = recordGrid.timeAtX(me.getX());
				int direction = (oldOffsetTime < newOffsetTime ? 1 : -1);
				float delta = (direction < 0 ? oldOffsetTime - newOffsetTime : newOffsetTime - oldOffsetTime);

				for(int rIdx:getSelectionModel().getSelectedIndices()) {
					Record selectedRecord = getRecordGrid().getSession().getRecord(rIdx);
					MediaSegment seg = selectedRecord.getSegment().getGroup(0);

					float st = (rIdx == getRecordGrid().getCurrentRecordIndex() ?
							currentRecordInterval.getStartMarker().getTime() : seg.getStartValue() / 1000.0f);
					float et = (rIdx == getRecordGrid().getCurrentRecordIndex() ?
							currentRecordInterval.getEndMarker().getTime() : seg.getEndValue() / 1000.0f);
					float intervalDuration = et - st;

					float newStartTime = 0.0f;
					float newEndTime = 0.0f;

					if (direction < 0) {
						newStartTime = Math.max(st - delta, getTimeModel().getStartTime());
						newEndTime = newStartTime + intervalDuration;
					} else {
						newEndTime = Math.min(et + delta, getTimeModel().getEndTime());
						newStartTime = newEndTime - intervalDuration;
					}
					if(rIdx == getRecordGrid().getCurrentRecordIndex()) {
						currentRecordInterval.getStartMarker().setTime(newStartTime);
						currentRecordInterval.getEndMarker().setTime(newEndTime);
					} else {
						MediaSegment newSeg = SessionFactory.newFactory().createMediaSegment();
						newSeg.setStartValue(newStartTime * 1000.0f);
						newSeg.setEndValue(newEndTime * 1000.0f);
						TierEdit<MediaSegment> tierEdit = new TierEdit<>(getParentView().getEditor(), selectedRecord.getSegment(), 0, newSeg);
						tierEdit.setFireHardChangeOnUndo(false);
						getParentView().getEditor().getUndoSupport().postEdit(tierEdit);
					}
				}
				dragData.isFirstChange = false;
			}
		}

	};

	/**
	 * The SplitMarker is used when splitting the current record
	 *
	 */
	public static class SplitMarker extends TimeUIModel.Marker {

		private Interval parentInterval;

		public SplitMarker(Interval parentInterval, float startTime) {
			super(startTime);
			setColor(UIManager.getColor(TimelineViewColors.SPLIT_MARKER_COLOR));
			setDraggable(true);

			this.parentInterval = parentInterval;
		}

		@Override
		public void setTime(float time) {
			if (time <= parentInterval.getStartMarker().getTime() || time >= parentInterval.getEndMarker().getTime())
				return;
			super.setTime(time);
		}

	}

}
