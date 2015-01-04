package ca.phon.session.impl;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.OrthoWordExtractor;
import ca.phon.orthography.Orthography;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.session.Word;

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
		return 
				(record.getOrthography().numberOfGroups() > groupIndex ? record.getOrthography().getGroup(groupIndex) 
						: null);
	}

	@Override
	public void setOrthography(Orthography ortho) {
		record.getOrthography().setGroup(groupIndex, ortho);
	}

	@Override
	public IPATranscript getIPATarget() {
		return 
				(record.getIPATarget().numberOfGroups() > groupIndex ?  record.getIPATarget().getGroup(groupIndex)
						: new IPATranscript());
	}

	@Override
	public IPATranscript getIPAActual() {
		return 
				(record.getIPAActual().numberOfGroups() > groupIndex ? record.getIPAActual().getGroup(groupIndex)
						: new IPATranscript());
	}
	
	@Override
	public Object getTier(String name) {
		final Tier<?> tier = record.getTier(name);
		int gIdx = groupIndex;
		if(!tier.isGrouped())
			gIdx = 0;
		final Object retVal = 
				(tier.numberOfGroups() > gIdx ? tier.getGroup(gIdx) : null);
		return retVal;
	}

	@Override
	public <T> T getTier(String name, Class<T> type) {
		final Tier<T> tier = record.getTier(name, type);
		int gIdx = groupIndex;
		if(!tier.isGrouped())
			gIdx = 0;
		final T retVal = 
				(tier.numberOfGroups() > gIdx ? tier.getGroup(gIdx) : null);
		return retVal;
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
		final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
		return (alignmentTier.numberOfGroups() > groupIndex ? alignmentTier.getGroup(groupIndex) : null);
	}

	@Override
	public void setPhoneAlignment(PhoneMap alignment) {
		record.getPhoneAlignment().setGroup(groupIndex, alignment);
	}

	@Override
	public String getNotes() {
		return 
				(record.getNotes().numberOfGroups() > 0 ? record.getNotes().getGroup(0) : null);
	}

	@Override
	public void setNotes(String notes) {
		record.getNotes().setGroup(0, notes);
	}

	@Override
	public <T> void setTier(String name, Class<T> type, T val) {
		final Tier<T> tier = record.getTier(name, type);
		if(tier != null) {
			if(tier.isGrouped())
				tier.setGroup(groupIndex, val);
			else
				tier.setGroup(0, val);
		}
	}
	
	@Override
	public Word getAlignedWord(int wordIndex) {
		return new WordImpl(getRecord(), groupIndex, wordIndex);
	}
	
	@Override
	public int getAlignedWordCount() {
		final Orthography ortho = getOrthography();
		final OrthoWordExtractor extractor = new OrthoWordExtractor();
		ortho.accept(extractor);
		return extractor.getWordList().size();
	}

}
