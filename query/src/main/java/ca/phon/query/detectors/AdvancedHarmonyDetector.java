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
package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.alignment.*;
import ca.phon.ipa.features.*;

/**
 * Harmony detector for query api.
 * 
 *
 */
public class AdvancedHarmonyDetector extends BasicHarmonyDetector {
	
	enum HarmonyType {
		Consonant,
		Vowel;
		
		public static HarmonyType fromOrdinal(int idx) {
			return HarmonyType.values()[idx];
		}
	}
	
	enum Directionality {
		Progressive,
		Regressive,
		Both;
	}
	
	/* Detector params */
	private HarmonyType type;
	private Directionality directionality;
	private FeatureSet sharedFeatures;
	private FeatureSet absentSharedFeatures;
	private FeatureSet neutralizedFeatures;
	private FeatureSet absentNeutralizedFeatures;
	
	/**
	 * Create a new harmony dectector of the specified type.
	 * 
	 * @param type 0 for consonant harmony, 1 for vowel
	 */
	public AdvancedHarmonyDetector(int type) {
		this(type, Directionality.Both);
	}
	
	public AdvancedHarmonyDetector(int type, Directionality dir) {
		this(type, dir, null, null);
	}
	
	public AdvancedHarmonyDetector(int type, Directionality dir, FeatureSet fsShared, FeatureSet fsNeutralized) {
		super(type == HarmonyType.Consonant.ordinal());
		
		this.type = HarmonyType.fromOrdinal(type);
		this.directionality = dir;
		this.sharedFeatures = fsShared;
		this.neutralizedFeatures = fsNeutralized;
	}
	
	
	
	@Override
	public Collection<DetectorResult> detect(PhoneMap pm) {
		final Collection<DetectorResult> potentialResults = super.detect(pm);
		final List<DetectorResult> retVal = new ArrayList<DetectorResult>();
		
		for(DetectorResult potentialResult:potentialResults) {
			boolean addResult = true;
			final HarmonyDetectorResult r = (HarmonyDetectorResult)potentialResult;
			
			if(directionality != Directionality.Both) {
				Directionality resultDirection = 
					(r.getFirstPosition() <= r.getSecondPosition() ? Directionality.Progressive : Directionality.Regressive);
				if(resultDirection != directionality) {
					addResult = false;
				}
			}
			
			if(addResult && sharedFeatures != null && sharedFeatures.size() > 0) {
				FeatureSet fsShared = r.getSharedFeatures();
				FeatureSet intersectSet = FeatureSet.intersect(sharedFeatures, fsShared);
				if(!intersectSet.equals(sharedFeatures)) {
					addResult = false;
				}
			}
			
			if(addResult && absentSharedFeatures != null && absentSharedFeatures.size() > 0) {
				FeatureSet fsShared = r.getSharedFeatures();
				FeatureSet intersectSet = FeatureSet.intersect(absentSharedFeatures, fsShared);
				if(intersectSet.size() > 0) {
					addResult = false;
				}
			}
			
			if(addResult && neutralizedFeatures != null && neutralizedFeatures.size() > 0) {
				FeatureSet fsNeutralized = r.getNeutralizedFeatures();
				FeatureSet intersectSet = FeatureSet.intersect(neutralizedFeatures, fsNeutralized);
				if(!intersectSet.equals(neutralizedFeatures)) {
					addResult = false;
				}
			}
			
			if(addResult && absentNeutralizedFeatures != null && absentNeutralizedFeatures.size() > 0) {
				FeatureSet fsNeutralized = r.getNeutralizedFeatures();
				FeatureSet intersectSet = FeatureSet.intersect(absentNeutralizedFeatures, fsNeutralized);
				if(intersectSet.size() > 0) {
					addResult = false;
				}
			}
			
			if(addResult) retVal.add(r);
		}
		
		return retVal;
	}

	/* Get/Set methods */
	public HarmonyType getType() {
		return type;
	}

	public void setType(HarmonyType type) {
		this.type = type;
	}

	public Directionality getDirectionality() {
		return directionality;
	}

	public void setDirectionality(Directionality directionality) {
		this.directionality = directionality;
	}
	
	public FeatureSet getAbsentSharedFeatures() {
		return absentSharedFeatures;
	}

	public void setAbsentSharedFeatures(FeatureSet absentSharedFeatures) {
		this.absentSharedFeatures = absentSharedFeatures;
	}

	public FeatureSet getAbsentNeutralizedFeatures() {
		return absentNeutralizedFeatures;
	}

	public void setAbsentNeutralizedFeatures(FeatureSet absentNeutralizedFeatures) {
		this.absentNeutralizedFeatures = absentNeutralizedFeatures;
	}

	public int getDirection() {
		return directionality.ordinal();
	}

	public void setDirection(int directionality) {
		this.directionality = Directionality.values()[directionality];
	}


	public FeatureSet getSharedFeatures() {
		return sharedFeatures;
	}

	public void setSharedFeatures(FeatureSet sharedFeatures) {
		this.sharedFeatures = sharedFeatures;
	}

	public FeatureSet getNeutralizedFeatures() {
		return neutralizedFeatures;
	}

	public void setNeutralizedFeatures(FeatureSet neutralizedFeatures) {
		this.neutralizedFeatures = neutralizedFeatures;
	}


}
