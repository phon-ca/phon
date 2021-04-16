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
import java.util.*;
import java.util.List;
import java.util.function.*;

import javax.swing.*;
import javax.swing.event.*;

import ca.phon.app.session.editor.view.timeline.actions.*;
import ca.phon.media.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.*;
import ca.phon.util.*;

public class RecordGrid extends TimeComponent {
	
	private Session session;

	private ListSelectionModel selectionModel = new DefaultListSelectionModel();

	private Set<Participant> speakerSet = new LinkedHashSet<>();
	
	private Set<String> tierSet = new LinkedHashSet<>();
	
	private int currentRecordIndex = -1;
	
	private Insets tierInsets = new Insets(2, 2, 2, 2);
	
	private final List<RecordGridMouseListener> listeners = Collections.synchronizedList(new ArrayList<>());
	
	private final List<BiConsumer<Participant, MenuBuilder>> participantMenuHandlers = Collections.synchronizedList(new ArrayList<>());
	
	private final static String uiClassId = "RecordGridUI";

	/** Size added to fontSize for each tier */
	private float fontSizeDelta = 0.0f;

	/* 
	 * Split mode.
	 * 
	 * During split mode, the current record will be replaced with the
	 * given left/right records
	 */
	private transient boolean splitMode = false;
	
	private transient Record leftRecordSplit = null;
	
	private transient Record rightRecordSplit = null;
	
	private transient boolean splitModeAccept = false;
	
	public RecordGrid(Session session) {
		this(new TimeUIModel(), session);
	}
	
	public RecordGrid(TimeUIModel timeModel, Session session) {
		super(timeModel);
		this.session = session;
		
		setOpaque(true);
		setBackground(Color.WHITE);
		
		updateUI();
	}

	public float getFontSizeDelta() {
		return this.fontSizeDelta;
	}

	public void setFontSizeDelta(float fontSizeDelta) {
		float oldValue = this.fontSizeDelta;
		this.fontSizeDelta = fontSizeDelta;
		firePropertyChange("fontSizeDelta", oldValue, fontSizeDelta);
	}

	public void addSpeaker(Participant speaker) {
		var currentSpeakerCount = speakerSet.size();
		speakerSet.add(speaker);
		firePropertyChange("speakerCount", currentSpeakerCount, speakerSet.size());
	}
	
	public void removeSpeaker(Participant speaker) {
		var currentSpeakerCount = speakerSet.size();
		speakerSet.remove(speaker);
		firePropertyChange("speakerCount", currentSpeakerCount, speakerSet.size());
	}
	
	public void setSpeakers(Collection<Participant> speakers) {
		var currentSpeakerCount = speakerSet.size();
		speakerSet.clear();
		speakerSet.addAll(speakers);
		firePropertyChange("speakerCount", currentSpeakerCount, speakerSet.size());
	}
	
	public void clearSpeakers() {
		var currentSpeakerCount = speakerSet.size();
		speakerSet.clear();
		firePropertyChange("speakerCount", currentSpeakerCount, speakerSet.size());
	}
	
	public void addTier(String tierName) {
		var tierCount = tierSet.size();
		tierSet.add(tierName);
		firePropertyChange("tierCount", tierCount, tierSet.size());
	}
	
	public void removeTier(String tierName) {
		var tierCount = tierSet.size();
		tierSet.remove(tierName);
		firePropertyChange("tierCount", tierCount, tierSet.size());
	}
	
	public void setTiers(Collection<String> tierNames) {
		var currentTierCount = tierSet.size();
		tierSet.clear();
		tierSet.addAll(tierNames);
		firePropertyChange("tierCount", currentTierCount, tierSet.size());
	}
	
	public Insets getTierInsets() {
		return this.tierInsets;
	}
	
	public void setTierInsets(Insets insets) {
		var oldInsets = this.tierInsets;
		this.tierInsets = insets;
		firePropertyChange("tierInsets", oldInsets, insets);
	}
	
	public List<Participant> getSpeakers() {
		return Collections.unmodifiableList(new ArrayList<>(speakerSet));
	}
	
	public List<String> getTiers() {
		return Collections.unmodifiableList(new ArrayList<>(tierSet));
	}

	public ListSelectionModel getSelectionModel() { return selectionModel; }

	public int getCurrentRecordIndex() {
		return this.currentRecordIndex;
	}
	
	public void setCurrentRecordIndex(int recordIndex) {
		var oldVal = this.currentRecordIndex;
		this.currentRecordIndex = recordIndex;

		//selectionModel.setSelectionInterval(recordIndex, recordIndex);

		super.firePropertyChange("currentRecordIndex", oldVal, recordIndex);
	}
	
	public Record getCurrentRecord() {
		return (this.currentRecordIndex >= 0 && this.currentRecordIndex < session.getRecordCount() ? 
				session.getRecord(currentRecordIndex) : null);
	}
	
	public void setCurrentRecord(Record r) {
		int rIdx = session.getRecordPosition(r);
		if(rIdx < 0)
			throw new IllegalArgumentException("Record not part of session");
		setCurrentRecordIndex(rIdx);
	}
	
	/**
	 * Add a listener to particpant pop-up menus.
	 * 
	 * @param listener
	 */
	public void addParticipantMenuHandler(BiConsumer<Participant, MenuBuilder> listener) {
		participantMenuHandlers.add(listener);
	}
	
	public void removeParticipantMenuHandler(BiConsumer<Participant, MenuBuilder> listener) {
		participantMenuHandlers.remove(listener);
	}
	
	public List<BiConsumer<Participant, MenuBuilder>> getParticipantMenuHandlers() {
		return this.participantMenuHandlers;
	}
	
	/**
	 * Setup speaker menu for given participant
	 * 
	 * @param participant
	 */
	public void setupParticipantMenu(Participant participant, MenuBuilder builder) {
		final PhonUIAction removeSpeakerAct = new PhonUIAction(this, "removeSpeaker", participant);
		removeSpeakerAct.putValue(PhonUIAction.NAME, "Hide participant");
		removeSpeakerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Hide " + participant + " tier from view");
		builder.addItem(".", removeSpeakerAct);

		for(var menuHandler:getParticipantMenuHandlers()) {
			menuHandler.accept(participant, builder);
		}
	}
	
	public boolean isSplitMode() {
		return this.splitMode;
	}
	
	public void setSplitMode(boolean splitMode) {
		var oldVal = this.splitMode;
		this.splitMode = splitMode;
		firePropertyChange("splitMode", oldVal, splitMode);
	}
	
	public boolean isSplitModeAccept() {
		return this.splitModeAccept;
	}
	
	public void setSplitModeAccept(boolean v) {
		this.splitModeAccept = v;
	}
	
	public void toggleSplitMode() {
		setSplitMode(!this.splitMode);
	}

	/**
	 * Setup split mode - only fires a single property change event
	 * @param leftRecord
	 * @param rightRecord
	 */
	public void beginSplitMode(Record leftRecord, Record rightRecord) {
		this.leftRecordSplit = leftRecord;
		this.rightRecordSplit = rightRecord;
		setSplitMode(true);
	}
	
	public Record getLeftRecordSplit() {
		return this.leftRecordSplit;
	}
	
	public void setLeftRecordSplit(Record record) {
		var oldVal = this.leftRecordSplit;
		this.leftRecordSplit = record;
		firePropertyChange("leftRecordSplit", oldVal, record);
	}
	
	public Record getRightRecordSplit() {
		return this.rightRecordSplit;
	}
	
	public void setRightRecordSplit(Record record) {
		var oldVal = this.rightRecordSplit;
		this.rightRecordSplit = record;
		firePropertyChange("rightRecordSplit", oldVal, record);
	}

	public void addRecordGridMouseListener(RecordGridMouseListener listener) {
		listeners.add(listener);
	}
	
	public void removeRecordGridMouseListener(RecordGridMouseListener listener) {
		listeners.remove(listener);
	}

	public void repaintRecord(int recordIndex) {
		if(recordIndex >= 0 && recordIndex < session.getRecordCount()) {
			Record r = session.getRecord(recordIndex);
			MediaSegment segment = r.getSegment().getGroup(0);
			super.repaint(segment.getStartValue()/1000.0f, segment.getEndValue()/1000.0f);
		}
	}

	public void fireRecordClicked(int recordIndex, MouseEvent me) {
		listeners.forEach( (l) -> l.recordClicked(recordIndex, me) );
	}
	
	public void fireRecordPressed(int recordIndex, MouseEvent me) {
		listeners.forEach( (l) -> l.recordPressed(recordIndex, me) );
	}
	
	public void fireRecordReleased(int recordIndex, MouseEvent me) {
		listeners.forEach( (l) -> l.recordReleased(recordIndex, me) );
	}
	
	public void fireRecordEntered(int recordIndex, MouseEvent me) {
		listeners.forEach( (l) -> l.recordEntered(recordIndex, me) );
	}
	
	public void fireRecordExited(int recordIndex, MouseEvent me) {
		listeners.forEach( (l) -> l.recordExited(recordIndex, me) );
	}
	
	public void fireRecordDragged(int recordIndex, MouseEvent me) {
		listeners.forEach( (l) -> l.recordDragged(recordIndex, me) );
	}

	public String getUIClassID() {
		return uiClassId;
	}
	
	@Override
	public void updateUI() {
		setUI(new DefaultRecordGridUI());
	}

	public DefaultRecordGridUI getUI() {
		return (DefaultRecordGridUI)ui;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public void setSession(Session session) {
		var oldVal = this.session;
		this.session = session;
		super.firePropertyChange("session", oldVal, session);
	}
	
	public Dimension getPreferredSize() {
		return getUI().getPreferredSize(this);
	}

	/**
	 * 'Ghost' markers are markers which are only visible
	 * when the mouse hovers over them.
	 */
	public static class GhostMarker extends TimeUIModel.Marker {

		private boolean isStart = false;
		
		public GhostMarker(float startTime) {
			super(startTime);
		}
		
		public boolean isStart() {
			return this.isStart;
		}
		
		public void setStart(boolean start) {
			this.isStart = start;
		}
		
	}
	
}
