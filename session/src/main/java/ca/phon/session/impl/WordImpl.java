package ca.phon.session.impl;

import java.lang.ref.WeakReference;
import java.util.List;

import ca.phon.alignment.AlignmentMap;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWordExtractor;
import ca.phon.orthography.Orthography;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Word;

public class WordImpl implements Word {
	
	// record
	private final WeakReference<Record> recordRef;
	
	// group
	private final int groupIndex;
	
	// word index
	private final int wordIndex;
	
	public WordImpl(Record record, int groupIndex, int wordIndex) {
		super();
		this.recordRef = new WeakReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = wordIndex;
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
	public int getWordIndex() {
		return this.wordIndex;
	}

	@Override
	public OrthoElement getOrthography() {
		final Orthography ortho = getGroup().getOrthography();
		final OrthoWordExtractor extractor = new OrthoWordExtractor();
		ortho.accept(extractor);
		
		final List<OrthoElement> wordList = extractor.getWordList();
		
		if(wordIndex >= 0 && wordIndex < wordList.size()) {
			return wordList.get(wordIndex);
		} else {
			return null;
		}
	}
	
	@Override
	public int getOrthographyWordLocation() {
		int retVal = -1;
		
		final OrthoElement ele = getOrthography();
		if(ele != null) {
			final int idx = getGroup().getOrthography().indexOf(ele);
			if(idx >= 0) {
				retVal = 0;
				for(int i = 0; i < idx; i++) {
					retVal += (i > 0 ? 1 : 0) + getGroup().getOrthography().elementAt(i).toString().length();
				}
				if(idx > 0) retVal++;
			}
		}
		
		return retVal;
	}

	@Override
	public IPATranscript getIPATarget() {
		final IPATranscript ipaTarget = getGroup().getIPATarget();
		final List<IPATranscript> wordList = ipaTarget.words();
		
		if(wordIndex >= 0 && wordIndex < wordList.size()) {
			return wordList.get(wordIndex);
		} else {
			return null;
		}
	}
	
	@Override
	public int getIPATargetWordLocation() {
		int retVal = -1;
	
		final IPATranscript target = getGroup().getIPATarget();
		final IPATranscript ipa = getIPATarget();
		if(ipa != null) {
			final int eleIdx = target.indexOf(ipa);
			retVal = target.stringIndexOfElement(eleIdx);
		}

		return retVal;
	}

	@Override
	public IPATranscript getIPAActual() {
		final IPATranscript ipaActual = getGroup().getIPAActual();
		final List<IPATranscript> wordList = ipaActual.words();
		
		if(wordIndex >= 0 && wordIndex < wordList.size()) {
			return wordList.get(wordIndex);
		} else {
			return null;
		}
	}
	
	@Override
	public int getIPAActualWordLocation() {
		int retVal = -1;
		
		final IPATranscript actual = getGroup().getIPAActual();
		final IPATranscript ipa = getIPAActual();
		if(ipa != null) {
			final int eleIdx = actual.indexOf(ipa);
			retVal = actual.stringIndexOfElement(eleIdx);
		}
	
		return retVal;
	}
	
	@Override
	public PhoneMap getPhoneAlignment() {
		final IPATranscript ipaT = getIPATarget();
		final IPATranscript ipaA = getIPAActual();
		
		final PhoneMap retVal = new PhoneMap(ipaT, ipaA);
		final IPATranscript filteredT = ipaT.removePunctuation();
		final IPATranscript filteredA = ipaA.removePunctuation();
		
		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		
		final int ipaTAlignStart =
				(filteredT.length() > 0 ? grpAlignment.getTopAlignmentElements().indexOf(filteredT.elementAt(0)) : -1);
		final int ipaAAlignStart = 
				(filteredA.length() > 0 ? grpAlignment.getBottomAlignmentElements().indexOf(filteredA.elementAt(0)) : -1);
		final int alignStart = Math.min(ipaTAlignStart, ipaAAlignStart);
		
		final int ipaTAlignEnd = 
				(filteredT.length() > 0 ? grpAlignment.getTopAlignmentElements().indexOf(filteredT.elementAt(filteredT.length()-1)) : -1);
		final int ipaAAlignEnd =
				(filteredA.length() > 0 ? grpAlignment.getBottomAlignmentElements().indexOf(filteredA.elementAt(filteredA.length()-1)) : -1);
		final int alignEnd = Math.max(ipaTAlignEnd, ipaAAlignEnd);
		
		if(alignStart >= 0 && alignEnd >= alignStart) {
			final int alignLen = alignEnd - alignStart + 1;
			
			final Integer topElements[] = new Integer[alignLen];
			final Integer btmElements[] = new Integer[alignLen];
			
			// copy alignment, but don't keep elements which are not
			// part of our word transcripts
			for(int i = 0; i < alignLen; i++) {
				final List<IPAElement> alignedPair = grpAlignment.getAlignedElements(alignStart+i);
				final IPAElement tEle = alignedPair.get(0);
				final IPAElement aEle = alignedPair.get(1);
				
				final Integer tIdx = 
						(tEle == null ? AlignmentMap.INDEL_VALUE : filteredT.indexOf(tEle));
				final Integer aIdx = 
						(aEle == null ? AlignmentMap.INDEL_VALUE : filteredA.indexOf(aEle));
				
				topElements[i] = tIdx;
				btmElements[i] = aIdx;
			}
			
			retVal.setTopAlignment(topElements);
			retVal.setBottomAlignment(btmElements);
		}
		
		return retVal;
	}

	@Override
	public String getNotes() {
		final String notes = getGroup().getNotes();
		final String[] wordList = notes.split("\\p{Space}");
		
		if(wordIndex >= 0 && wordIndex < wordList.length) {
			return wordList[wordIndex];
		} else {
			return null;
		}
	}

	@Override
	public int getNotesWordLocation() {
		int retVal = -1;
		
		final String notes = getGroup().getNotes();
		final String[] wordList = notes.split("\\p{Space}");
		
		if(wordIndex >=0 && wordIndex < wordList.length) {
			int currentIdx = 0;
			for(int i = 0; i < wordIndex; i++) {
				currentIdx += (i > 0 ? 1 : 0) + wordList[i].length();
			}
			retVal = currentIdx;
		}
		
		return retVal;
	}
	
	@Override
	public Object getTier(String name) {
		Object retVal = null;
		
		// check for system tier
		final SystemTierType systemTier = SystemTierType.tierFromString(name);
		if(systemTier != null) {
			switch(systemTier) {
			case Orthography:
				retVal = getOrthography();
				break;
				
			case IPATarget:
				retVal = getIPATarget();
				break;
				
			case IPAActual:
				retVal = getIPAActual();
				break;
				
			case Notes:
				retVal = getNotes();
				break;
				
			default:
				break;
			}
		}
		
		if(retVal == null) {
			final String tierValue = getGroup().getTier(name, String.class);
			final String wordList[] = tierValue.split("\\p{Space}");
			
			if(wordIndex >= 0 && wordIndex < wordList.length)
				retVal = wordList[wordIndex];
		}
		
		return retVal;
	}

	@Override
	public int getTierWordLocation(String tierName) {
		int retVal = -1;
		
		final String tierString = getGroup().getTier(tierName, String.class);
		final String wordList[] = tierString.split("\\p{Space}");
		
		if(wordIndex >= 0 && wordIndex < wordList.length) {
			int currentIndex = 0;
			for(int i = 0; i < wordIndex; i++) {
				currentIndex += (i > 0 ? 1 : 0) + wordList[i].length();
			}
			retVal = currentIndex;
		}
		
		return retVal;
	}
}
