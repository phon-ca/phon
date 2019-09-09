package ca.phon.app.session.editor.view.timeline;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import ca.phon.app.media.TimeComponent;
import ca.phon.app.media.TimeUIModel;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class RecordGrid extends TimeComponent {
	
	private Session session;
	
	private Set<Participant> speakerSet = new LinkedHashSet<>();
	
	private Set<String> tierSet = new LinkedHashSet<>();
	
	private Record currentRecord = null;
	
	private Insets tierInsets = new Insets(2, 2, 2, 2);
	
	private final List<RecordGridMouseListener> listeners = Collections.synchronizedList(new ArrayList<>());
	
	private final static String uiClassId = "RecordGridUI";
	
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
	
	public Record getCurrentRecord() {
		return this.currentRecord;
	}
	
	public void setCurrentRecord(Record record) {
		var oldVal = this.currentRecord;
		this.currentRecord = record;
		super.firePropertyChange("currentRecord", oldVal, record);
	}
	
	public void addRecordGridMouseListener(RecordGridMouseListener listener) {
		listeners.add(listener);
	}
	
	public void removeRecordGridMouseListener(RecordGridMouseListener listener) {
		listeners.remove(listener);
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
	protected void setUI(ComponentUI newUI) {
		super.setUI(newUI);
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
