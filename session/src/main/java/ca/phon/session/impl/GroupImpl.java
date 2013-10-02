package ca.phon.session.impl;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Tier;

public class GroupImpl implements Group {
	
	/**
	 * Weak reference to parent record
	 */
	private final Record record;

	/**
	 * Group index
	 */
	private final int groupIndex;
	
	GroupImpl(Record record, int idx) {
		super();
		this.record = record;
		this.groupIndex = idx;
	}
	
	public Record getRecord() {
		return this.record;
	}
	
	public int getGroupIndex() {
		return this.groupIndex;
	}
	
	@Override
	public Orthography getOrthography() {
		return record.getOrthography().getGroup(groupIndex);
	}

	@Override
	public void setOrthography(Orthography ortho) {
		record.getOrthography().setGroup(groupIndex, ortho);
	}

	@Override
	public IPATranscript getIPATarget() {
		return record.getIPATarget().getGroup(groupIndex);
	}

	@Override
	public IPATranscript getIPAActual() {
		return record.getIPAActual().getGroup(groupIndex);
	}
	
	@Override
	public Object getTier(String name) {
		return record.getTier(name).getGroup(groupIndex);
	}

	@Override
	public <T> T getTier(String name, Class<T> type) {
		return record.getTier(name, type).getGroup(groupIndex);
	}

	@Override
	public void setIPATarget(IPATranscript ipa) {
		record.getIPATarget().setGroup(groupIndex, ipa);
	}

	@Override
	public void setIPAActual(IPATranscript ipa) {
		record.getIPAActual().setGroup(groupIndex, ipa);
	}

	@Override
	public PhoneMap getPhoneAlignment() {
		return record.getPhoneAlignment().getGroup(groupIndex);
	}

	@Override
	public void setPhoneAlignment(PhoneMap alignment) {
		record.getPhoneAlignment().setGroup(groupIndex, alignment);
	}

	@Override
	public String getNotes() {
		return record.getNotes().getGroup(0);
	}

	@Override
	public void setNotes(String notes) {
		record.getNotes().setGroup(groupIndex, notes);
	}

	@Override
	public <T> void setTier(String name, Class<T> type, T val) {
		final Tier<T> tier = record.getTier(name, type);
		if(tier != null) {
			tier.setGroup(groupIndex, val);
		}
	}

}
