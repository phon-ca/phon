package ca.phon.session.impl;

import java.util.concurrent.atomic.AtomicReference;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.alignment.SyllableMap;
import ca.phon.session.AlignedSyllable;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Word;

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
		int retVal = -1;
		
		final IPATranscript target = getGroup().getIPATarget();
		if(target != null) {
			final IPATranscript ipa = getIPATarget();
		
			if(ipa != null) {
				final int eleIdx = target.indexOf(ipa);
				retVal = target.stringIndexOfElement(eleIdx);
			}
		}
		
		return retVal;
	}
	
	@Override
	public int getIPAActualLocation() {
		int retVal = -1;
		
		final IPATranscript actual = getGroup().getIPAActual();
		if(actual != null) {
			final IPATranscript ipa = getIPAActual();
		
			if(ipa != null) {
				final int eleIdx = actual.indexOf(ipa);
				retVal = actual.stringIndexOfElement(eleIdx);
			}
		}
		
		return retVal;
	}

	@Override
	public PhoneMap getPhoneAlignment() {
		final IPATranscript ipaT = (getIPATarget() == null ? new IPATranscript() : getIPATarget());
		final IPATranscript ipaA = (getIPAActual() == null ? new IPATranscript() : getIPAActual());

		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		if(grpAlignment == null) new PhoneMap();

		return grpAlignment.getSubAlignment(ipaT, ipaA);
	}
	
	@Override
	public int getPhoneAlignmentLocation() {
		final IPATranscript ipaT = (getIPATarget() == null ? new IPATranscript() : getIPATarget());
		final IPATranscript ipaA = (getIPAActual() == null ? new IPATranscript() : getIPAActual());

		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		if(grpAlignment == null) return -1;
		
		return grpAlignment.getSubAlignmentIndex(ipaT, ipaA);
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
