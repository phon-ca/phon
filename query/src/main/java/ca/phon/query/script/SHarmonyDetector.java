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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.phon.application.transcript.IWord;
import ca.phon.engines.detectors.DetectorResult;
import ca.phon.engines.detectors.HarmonyDetector;
import ca.phon.featureset.FeatureSet;

/**
 * Harmony detector for query api.
 * 
 *
 */
public class SHarmonyDetector implements SDetector {
	
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

	/** Detector */
	private HarmonyDetector detector;
	
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
	public SHarmonyDetector(int type) {
		this(type, Directionality.Both);
	}
	
	public SHarmonyDetector(int type, Directionality dir) {
		this(type, dir, null, null);
	}
	
	public SHarmonyDetector(int type, Directionality dir, FeatureSet fsShared, FeatureSet fsNeutralized) {
		super();
		
		this.type = HarmonyType.fromOrdinal(type);
		this.directionality = dir;
		this.sharedFeatures = fsShared;
		this.neutralizedFeatures = fsNeutralized;
		
		createDetector();
	}
	
	private void createDetector() {
		detector = new HarmonyDetector(
				this.type == HarmonyType.Consonant );
	}
	
	@Override
	public SDetectorResult[] detect(SRecord record, int group) {
		List<SDetectorResult> retVal = new ArrayList<SDetectorResult>();
		
		if(group < 0 || group >= record.getNumberOfGroups()) {
			return new SDetectorResult[0];
		}
		
		IWord grp = record._getUtt().getWords().get(group);
		Collection<DetectorResult> detectedResults = detector.detect(grp);
		
		// filter results
		for(DetectorResult r:detectedResults) {
			boolean addResult = true;
			
			if(directionality != Directionality.Both) {
				Directionality resultDirection = 
					(r.getFirstPosition() <= r.getSecondPosition() ? Directionality.Progressive : Directionality.Regressive);
				if(resultDirection != directionality) {
					addResult = false;
				}
			}
			
			if(addResult && sharedFeatures != null && sharedFeatures.size() > 0) {
				FeatureSet fsShared = r.getFeatures1();
				FeatureSet intersectSet = FeatureSet.intersect(sharedFeatures, fsShared);
				if(!intersectSet.equals(sharedFeatures)) {
					addResult = false;
				}
			}
			
			if(addResult && absentSharedFeatures != null && absentSharedFeatures.size() > 0) {
				FeatureSet fsShared = r.getFeatures1();
				FeatureSet intersectSet = FeatureSet.intersect(absentSharedFeatures, fsShared);
				if(intersectSet.size() > 0) {
					addResult = false;
				}
			}
			
			if(addResult && neutralizedFeatures != null && neutralizedFeatures.size() > 0) {
				FeatureSet fsNeutralized = r.getFeatures2();
				FeatureSet intersectSet = FeatureSet.intersect(neutralizedFeatures, fsNeutralized);
				if(!intersectSet.equals(neutralizedFeatures)) {
					addResult = false;
				}
			}
			
			if(addResult && absentNeutralizedFeatures != null && absentNeutralizedFeatures.size() > 0) {
				FeatureSet fsNeutralized = r.getFeatures2();
				FeatureSet intersectSet = FeatureSet.intersect(absentNeutralizedFeatures, fsNeutralized);
				if(intersectSet.size() > 0) {
					addResult = false;
				}
			}
			
			if(addResult)
				retVal.add(new SDetectorResult(record._getUtt(), record._getUttIndex(), group, r));
		}
		
		return retVal.toArray(new SDetectorResult[0]);
	
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
