package ca.phon.session.impl;

import java.util.concurrent.atomic.AtomicReference;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.SyllableMap;
import ca.phon.session.*;

public class AlignedSyllableImpl extends AlignedSyllable {

	// record
	private final AtomicReference<Record> recordRef;

	// group
	private final int groupIndex;

	// word index
	private final int wordIndex;
	
	// syllable index
	private final int syllIndex;

	public AlignedSyllableImpl(Record record, int groupIndex, int syllIndex) {
		super();
		
		this.recordRef = new AtomicReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = -1;
		this.syllIndex = syllIndex;
	}
	
	public AlignedSyllableImpl(Record record, int groupIndex, int wordIndex, int syllIndex) {
		super();
		this.recordRef = new AtomicReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = wordIndex;
		this.syllIndex = syllIndex;
	}

	@Override
	public IPATranscript getIPATarget() {
		final SyllableMap syllableAlignment = 
				(getWordIndex() >= 0 ? getWord().getSyllableAlignment() : getGroup().getSyllableAlignment());
		if(getSyllableIndex() < syllableAlignment.getAlignmentLength()) {
			return syllableAlignment.getTopAlignmentElements().get(getSyllableIndex());
		} else {
			return null;
		}
	}

	@Override
	public IPATranscript getIPAActual() {
		final SyllableMap syllableAlignment = 
				(getWordIndex() >= 0 ? getWord().getSyllableAlignment() : getGroup().getSyllableAlignment());
		if(getSyllableIndex() < syllableAlignment.getAlignmentLength()) {
			return syllableAlignment.getBottomAlignmentElements().get(getSyllableIndex());
		} else {
			return null;
		}
	}

	@Override
	public int getIPATargetLocation() {
		if(getWordIndex() < 0) {
			return getGroup().getIPATarget().indexOf(getIPATarget());
		} else {
			final Word word = getWord();
			int wordStartLocation = word.getIPATargetWordLocation();
			final IPATranscript target = word.getIPATarget();
			
			return wordStartLocation + target.indexOf(getIPATarget());
		}
	}

	@Override
	public int getIPAActualLocation() {
		if(getWordIndex() < 0) {
			return getGroup().getIPAActual().indexOf(getIPAActual());
		} else {
			final Word word = getWord();
			int wordStartLocation = word.getIPAActualWordLocation();
			final IPATranscript actual = word.getIPAActual();
			return wordStartLocation + actual.indexOf(getIPAActual());
		}
	}

	@Override
	public Group getGroup() {
		final Record record = recordRef.get();
		if(record != null) {
			return record.getGroup(groupIndex);
		} else {
			return null;
		}
	}

	@Override
	public Word getWord() {
		return new WordImpl(recordRef.get(), groupIndex, wordIndex);
	}

	@Override
	public int getWordIndex() {
		return wordIndex;
	}

	@Override
	public int getSyllableIndex() {
		return syllIndex;
	}

	@Override
	public int getGroupIndex() {
		return groupIndex;
	}
	
}
