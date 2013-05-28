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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.phon.alignment.PhoneMap;
import ca.phon.application.transcript.IUtterance;
import ca.phon.engines.detectors.DetectorResult;
import ca.phon.engines.detectors.DetectorResultType;
import ca.phon.featureset.FeatureSet;
import ca.phon.gui.recordeditor.SystemTierType;
import ca.phon.phone.Phone;
import ca.phon.util.Range;

/**
 * Enclosing class for detector results in a script.
 * Auto-create proper metadata.
 * 
 */
public class SDetectorResult {
	
	
	enum HarmonyMetadata {
		Directionality,
		SharedFeatures,
		NeutralizedFeatures;
		
		String[] fieldNames = {
				"Directionality",
				"Shared Features",
				"Neutralized Features"
		};
		
		public String getName() {
			return fieldNames[ordinal()];
		}
	}
	
	enum MetathesisMetadata {
		Features1,
		Features2;
		
		String[] fieldNames = {
				"Features (P1 Target, P2 Actual):",
				"Features (P2 Target, P1 Actual):"
		};
		
		public String getName() {
			return fieldNames[ordinal()];
		}
	}
	
	/* The detector result */
	private DetectorResult result;
	
	/* Grp index */
	private int groupIndex;
	
	private int uttIndex;
	
	private IUtterance utt;
	
	public SDetectorResult(IUtterance utt, int uttIndex, int grp, DetectorResult r) {
		super();
		
		this.utt = utt;
		this.uttIndex = uttIndex;
		this.groupIndex = grp;
		this.result = r;
	}

	/**
	 * Get the result values for this result
	 * @return 4 result values {t1, t2, a1, a2}
	 */
	public ResultValue[] getResultValues() {
		ResultValue[] retVal = new ResultValue[4];
		
		// convert alignment positions to phone
		// positions
		int firstAlignPos = 
			(isLeftToRight() ? result.getFirstPosition() : result.getSecondPosition());
		int secAlignPos = 
			(isLeftToRight() ? result.getSecondPosition() : result.getFirstPosition());
		PhoneMap pm = result.getPhoneMap();
		List<Phone> targetPhones = pm.getTargetRep().getPhones();
		List<Phone> actualPhones = pm.getActualRep().getPhones();
		
		// target side
		Phone firstTargetPhone = 
			pm.getTopAlignmentElements().get(firstAlignPos);
		Range firstTargetPhoneRange =
			(firstTargetPhone == null ? new Range(0, 0, true) :  // indel
			new Range(firstTargetPhone.getPhoneIndex(), firstTargetPhone.getPhoneIndex())
			);
		Phone secondTargetPhone =
			pm.getTopAlignmentElements().get(secAlignPos);
		Range secondTargetPhoneRange =
			(secondTargetPhone == null ? new Range(0, 0, true) :
			new Range(secondTargetPhone.getPhoneIndex(), secondTargetPhone.getPhoneIndex())
			);
		
		// actual side
		Phone firstActualPhone =
			pm.getBottomAlignmentElements().get(firstAlignPos);
		Range firstActualPhoneRange =
			(firstActualPhone == null ? new Range(0, 0, true) : 
			new Range(firstActualPhone.getPhoneIndex(), firstActualPhone.getPhoneIndex())
			);
		Phone secondActualPhone =
			pm.getBottomAlignmentElements().get(secAlignPos);
		Range secondActualPhoneRange =
			(secondActualPhone == null ? new Range(0, 0, true) : 
			new Range(secondActualPhone.getPhoneIndex(), secondActualPhone.getPhoneIndex())
			);
		
		Range firstTargetRange = 
			Phone.convertPhoneRangetoStringRange(targetPhones, firstTargetPhoneRange);
		Range secondTargetRange =
			Phone.convertPhoneRangetoStringRange(targetPhones, secondTargetPhoneRange);
		
		Range firstActualRange =
			Phone.convertPhoneRangetoStringRange(actualPhones, firstActualPhoneRange);
		Range secondActualRange =
			Phone.convertPhoneRangetoStringRange(actualPhones, secondActualPhoneRange);
		
		// create the 4 result values
		retVal[0] = new SIPARange(utt,
				firstTargetRange,
				uttIndex,
				SystemTierType.IPATarget.getTierName(),
				groupIndex);
		retVal[1] = new SIPARange(utt,
				secondTargetRange,
				uttIndex,
				SystemTierType.IPATarget.getTierName(),
				groupIndex);
		
		retVal[2] = new SIPARange(utt,
				firstActualRange,
				uttIndex,
				SystemTierType.IPAActual.getTierName(),
				groupIndex);
		retVal[3] = new SIPARange(utt,
				secondActualRange,
				uttIndex,
				SystemTierType.IPAActual.getTierName(),
				groupIndex);
		
		return retVal;
	}
	
	private boolean isLeftToRight() {
		return result.getFirstPosition() <= result.getSecondPosition();
	}
	
	public Map<String, String> getMetadata() {
		Map<String, String> retVal = new TreeMap<String, String>();
		
		if(result.getType() == DetectorResultType.ConsonantHarmony
				|| result.getType() == DetectorResultType.VowelHarmony) {
			retVal = getHarmonyMetadata();
		} else if (result.getType() == DetectorResultType.Metathesis) {
			retVal = getMetathesisMetadata();
		}
		
		return retVal;
	}
	
	private Map<String, String> getHarmonyMetadata() {
		Map<String, String> retVal = new TreeMap<String, String>();
		
		for(HarmonyMetadata mf:HarmonyMetadata.values()) {
			if(mf == HarmonyMetadata.Directionality) {
				String val = 
					(isLeftToRight() ? "Progressive" : "Regressive");
				retVal.put(mf.getName(), val);
			} else if(mf == HarmonyMetadata.SharedFeatures) {
				FeatureSet fs = result.getFeatures1();
				retVal.put(mf.getName(), fs.toString());
			} else if(mf == HarmonyMetadata.NeutralizedFeatures) {
				FeatureSet fs = result.getFeatures2();
				retVal.put(mf.getName(), fs.toString());
			}
		}
		
		return retVal;
	}
	
	private Map<String, String> getMetathesisMetadata() {
		Map<String, String> retVal = new TreeMap<String, String>();
		
		for(MetathesisMetadata mf:MetathesisMetadata.values()) {
			if(mf == MetathesisMetadata.Features1) {
				FeatureSet fs = result.getFeatures1();
				retVal.put(mf.getName(), fs.toString());
			} else if(mf == MetathesisMetadata.Features2) {
				FeatureSet fs = result.getFeatures2();
				retVal.put(mf.getName(), fs.toString());
			}
		}
		
		return retVal;
	}
	
	public void setType(String t) {}
	
	public String getType() {
		return "DETECTOR";
	}
}
