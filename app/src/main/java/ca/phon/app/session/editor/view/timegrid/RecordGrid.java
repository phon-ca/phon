package ca.phon.app.session.editor.view.timegrid;

import java.awt.Color;
import java.awt.Dimension;
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

	private final static int DEFAULT_TIER_HEIGHT = 30;
	
	private int tierHeight = DEFAULT_TIER_HEIGHT;
	
	private Session session;
	
	private Set<Participant> speakerSet = new LinkedHashSet<>();
	
	private Set<String> tierSet = new LinkedHashSet<>();
	
	private Record currentRecord = null;
	
	private Collection<Record> highlightedRecords = new ArrayList<>();
	
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
	
	public int getTierHeight() {
		return this.tierHeight;
	}
	
	public void setTierHeight(int height) {
		var oldVal = this.tierHeight;
		this.tierHeight = height;
		super.firePropertyChange("tierHeight", oldVal, height);
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
	
}
