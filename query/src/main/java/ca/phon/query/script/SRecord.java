/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.query.script;

import ca.phon.application.transcript.IDepTierDesc;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.IUtterance;
import ca.phon.application.transcript.IWord;
import ca.phon.application.transcript.TranscriptUtils;
import ca.phon.gui.recordeditor.SystemTierType;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.phone.Phone;
import ca.phon.util.Range;

/**
 * The record object as used in searching.  This object
 * is used within the javascript environment.
 * 
 *
 */
public class SRecord {
	/** The index */
	private int uttIndex;
	
	/** The utterance */
	private IUtterance utt;
	
	private ITranscript transcript;
	
	/** Constructor */
	public SRecord(int uttIndex, IUtterance utt, ITranscript t) {
		super();
		
		this.uttIndex = uttIndex;
		this.utt = utt;
		this.transcript = t;
	}
	
	/* Access methods */
	/**
	 * Return the number of groups in the record.
	 * 
	 * @return the number of groups
	 */
	public int getNumberOfGroups() {
		// return the number of word groups
		return this.utt.getWords().size();
	}
	
	/**
	 * Returns the given tier's value as a string.
	 * 
	 * @param tierName
	 */
	public String getTierString(String tierName) {
		return this.utt.getTierString(tierName);
	}
	
	/**
	 * Get the group identified by it's tier
	 * and position.
	 * 
	 * @param tier
	 * @param gIndex
	 * @return the group if both tier exists, is a group tier, 
	 * and 0 < gindex < numberOfGroups(), <CODE>null</CODE> otherwise.
	 */
	public ScriptGroup getGroup(String tier, int gIndex) {
		if(gIndex < 0 || gIndex >= getNumberOfGroups())
			return null;
		// check tier type
		ScriptGroup retVal = null;
	
		IWord w = this.utt.getWords().get(gIndex);
		
		String tierVal = TranscriptUtils.getTierValue(w, tier);
		if(SystemTierType.IPAActual.getTierName().equals(tier)
				|| SystemTierType.IPATarget.getTierName().equals(tier)) {
			retVal = new SIPAGroup(utt, new Range(0, tierVal.length(), true),
					uttIndex, tier, gIndex);
		} else if(SystemTierType.Orthography.getTierName().equals(tier)
				|| isWordAlignedTier(tier)) {
			retVal = new SGroup(utt, new Range(0, tierVal.length(), true),
					uttIndex, tier, gIndex);
		} else {
			// it's a flat tier
			tierVal = TranscriptUtils.getTierValue(utt, tier);
			if(tierVal != null && gIndex == 0) {
				retVal = new FlatGroup(utt, new Range(0, tierVal.length(), true),
						uttIndex, tier);
			}
		}
		
		return retVal;
	}
	
	private boolean isWordAlignedTier(String tiername) {
		boolean retVal = false;
		for(IDepTierDesc depTier:transcript.getWordAlignedTiers()) {
			if(depTier.getTierName().equals(tiername))
				retVal = true;
		}
		return retVal;
	}
	
	/**
	 * Get the speaker of this record.
	 * 
	 * @return the participant
	 */
	public SParticipant getSpeaker() {
		return new SParticipant(utt.getSpeaker(), transcript);
	}
	
	/**
	 * Returns the phones aligned with the given phones.
	 * If tierName is IPA Target the return value is the aligned
	 * phones in IPA Actual and vice versa.
	 * 
	 * @param ResultValue
	 * @return the aligned phones.  <CODE>null</CODE> if the
	 * given result value is not from one of the IPA tiers
	 */
	public ResultValue getAlignedPhones(ResultValue rv) {
		if(rv == null) return null;
		
		if(!rv.getTier().equals(SystemTierType.IPATarget.getTierName())
				&& !rv.getTier().equals(SystemTierType.IPAActual.getTierName()))
			return null;
		
		IWord word = utt.getWords().get(rv.getGroupIndex());
		PhoneMap pm = word.getPhoneAlignment();
		
		String tierName = (rv.getTier().equals(SystemTierType.IPATarget.getTierName()) ?
				SystemTierType.IPAActual.getTierName() : SystemTierType.IPATarget.getTierName());
		
		// no alignment
		if(pm == null) return new SIPARange(utt, new Range(0, 0), 
				rv.getRecordIndex(), tierName, rv.getGroupIndex());
		
		String ipaString  =
			((SRange)getGroup(rv.getTier(), rv.getGroupIndex())).getData();
		
		Range phoneRange = 
			Phone.convertStringRangeToPhoneRange(ipaString, rv.getDataRange());
		
		Phone[] matchSide = 
			(rv.getTier().equals(SystemTierType.IPATarget.getTierName()) ?
					pm.getTopAlignmentElements().toArray(new Phone[0]) : 
					pm.getBottomAlignmentElements().toArray(new Phone[0]));
		Phone[] refSide = 
			(rv.getTier().equals(SystemTierType.IPATarget.getTierName()) ?
					pm.getBottomAlignmentElements().toArray(new Phone[0]) : 
					pm.getTopAlignmentElements().toArray(new Phone[0]));
		
		int firstPhoneIndex = -1;
		int lastPhoneIndex = -1;
		
		for(int i = 0; i < pm.getAlignmentLength(); i++) {
			Phone matchPhone = matchSide[i];
			Phone refPhone = refSide[i];
			
//			if(refPhone != null) {
//				if(phoneRange.contains(refPhone.getPhoneIndex())) {
//					if(matchPhone != null) {
//						if(firstPhoneIndex < 0) {
//							firstPhoneIndex = matchPhone.getPhoneIndex();
//						}
//						lastPhoneIndex = matchPhone.getPhoneIndex()+1;
//					}
//				} else {
//					if(firstPhoneIndex >= 0)
//						break;
//				}
//			}
			if(matchPhone != null) {
				if(phoneRange.contains(matchPhone.getPhoneIndex())) {
					if(refPhone != null) {
						if(firstPhoneIndex < 0)
							firstPhoneIndex = refPhone.getPhoneIndex();
						lastPhoneIndex = refPhone.getPhoneIndex()+1;
					}
				} else {
					if(firstPhoneIndex >= 0)
						break;
				}
			}
		}
		
		Range refPhoneRange = new Range(firstPhoneIndex, lastPhoneIndex, true);
		
		String oppIPAString = 
			((SRange)getGroup(tierName, rv.getGroupIndex())).getData();
		Range retRange = 
			Phone.convertPhoneRangetoStringRange(Phone.toPhoneList(oppIPAString), refPhoneRange);
		
		return new SIPARange(utt, retRange, rv.getRecordIndex(), tierName, rv.getGroupIndex());
	}
	
	/**
	 * Get alignment data for the given ResultValue.
	 * Alignment data is returned as a matrix of
	 * 2 colums.  Column 1 inlcludes the data from
	 * the tier in the given ResultValue.  Colume 2
	 * includes the data form the aligned tier.  These
	 * arrays will both be the same length.  Values
	 * of null indicate positions of indels.  While individual
	 * values can indeed be added as results, NULL values should
	 * not be included in result sets.
	 * 
	 * @param rv
	 * 
	 * @returns the alignment data.  NULL if rv is NULL
	 * or not data from an IPA tier.  Also returns NULL
	 * if no alignment data exists
	 */
	public SIPAPhone[][] getAlignmentData(ResultValue rv)
	{
		SIPAPhone[][] retVal = new SIPAPhone[2][];
		
		if(rv == null) return null;
		
		if(!rv.getTier().equals(SystemTierType.IPATarget.getTierName())
				&& !rv.getTier().equals(SystemTierType.IPAActual.getTierName()))
			return null;
		
		IWord word = utt.getWords().get(rv.getGroupIndex());
		PhoneMap pm = word.getPhoneAlignment();
		
		String tierName = (rv.getTier().equals(SystemTierType.IPATarget.getTierName()) ?
				SystemTierType.IPAActual.getTierName() : SystemTierType.IPATarget.getTierName());
		
		// no alignment
		if(pm == null) return null;
		
		String ipaString  =
			((SRange)getGroup(rv.getTier(), rv.getGroupIndex())).getData();
		String oppIPAString = 
			((SRange)getGroup(tierName, rv.getGroupIndex())).getData();
		
		Range phoneRange = 
			Phone.convertStringRangeToPhoneRange(ipaString, rv.getDataRange());
		
		Phone[] matchSide = 
			(rv.getTier().equals(SystemTierType.IPATarget.getTierName()) ?
					pm.getTopAlignmentElements().toArray(new Phone[0]) : 
					pm.getBottomAlignmentElements().toArray(new Phone[0]));
		Phone[] refSide = 
			(rv.getTier().equals(SystemTierType.IPATarget.getTierName()) ?
					pm.getBottomAlignmentElements().toArray(new Phone[0]) : 
					pm.getTopAlignmentElements().toArray(new Phone[0]));
		
		int startAlignIndex = -1;
		int endAlignIndex = -1;
		for(int i = 0; i < pm.getAlignmentLength(); i++) {
			Phone matchPhone = matchSide[i];
//			Phone refPhone = refSide[i];
			
			if(matchPhone != null) {
				if(phoneRange.contains(matchPhone.getPhoneIndex())) {
					
					if(startAlignIndex < 0)
						startAlignIndex = i;
					endAlignIndex = i+1;
					
				} else {
					if(startAlignIndex >= 0)
						break;
				}
			}
		}
		
		if(startAlignIndex >=0 && endAlignIndex >= 0) {
			int alignLen = endAlignIndex - startAlignIndex;
			retVal[0] = new SIPAPhone[alignLen];
			retVal[1] = new SIPAPhone[alignLen];
			
			int idx = startAlignIndex;
			for(int i = 0; i < alignLen; i++) {
				
				Phone matchPhone = matchSide[idx];
				SIPAPhone matchVal = (matchPhone == null ? null : new SIPAPhone(utt, 
						Phone.convertPhoneRangetoStringRange( Phone.toPhoneList(ipaString), new Range(matchPhone.getPhoneIndex(), matchPhone.getPhoneIndex(), false)),
						uttIndex,
						rv.getTier(),
						rv.getGroupIndex()));
				retVal[0][i] = matchVal;
				
				Phone refPhone = refSide[idx];
				SIPAPhone refVal = (refPhone == null ? null : new SIPAPhone(utt,
						Phone.convertPhoneRangetoStringRange( Phone.toPhoneList(oppIPAString) , new Range(refPhone.getPhoneIndex(), refPhone.getPhoneIndex(), false)),
						uttIndex,
						tierName,
						rv.getGroupIndex()));
				
				retVal[1][i] = refVal;
				idx++;
			}
		} else {
			return null;
		}
		
//		System.out.println(retVal);
		
		return retVal;
	}
	
	/**
	 * Get the number of the record
	 * 
	 * @return recordNumber
	 */
	public int getRecordNumber() {
		return this.uttIndex;
	}

	public int _getUttIndex() {
		return uttIndex;
	}

	public IUtterance _getUtt() {
		return utt;
	}
}
