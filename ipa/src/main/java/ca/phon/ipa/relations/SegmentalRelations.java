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
package ca.phon.ipa.relations;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.alignment.PhoneMap;

import java.util.*;

/**
 * Order of segmental relations: Reduplication, Migration, Metathesis, Harmony, Assimilation
 *
 */
public class SegmentalRelations {

	private boolean includeConsonants = true;
	
	private boolean includeVowels = true;
	
	private List<SegmentalRelationDetector> detectors = new ArrayList<>();
	
	public SegmentalRelations(boolean includeConsonants, boolean includeVowels) {
		super();
		this.includeConsonants = includeConsonants;
		this.includeVowels = includeVowels;
	}
	
	private void initDefaultDetectors() {
		detectors.add(new ReduplicationDetector());
		detectors.add(new MetathesisDetector());
		detectors.add(new MigrationDetector());
		detectors.add(new HarmonyDetector());
		detectors.add(new AssimilationDetector());
	}
	
	public void addDetector(SegmentalRelationDetector detector) {
		detectors.add(detector);
	}
	
	public void removeDetector(SegmentalRelationDetector detector) {
		detectors.remove(detector);
	}
	
	public List<SegmentalRelationDetector> getDetectors() {
		return Collections.unmodifiableList(detectors);
	}
	
	public boolean isIncludeConsonants() {
		return includeConsonants;
	}

	public void setIncludeConsonants(boolean includeConsonants) {
		this.includeConsonants = includeConsonants;
	}

	public boolean isIncludeVowels() {
		return includeVowels;
	}

	public void setIncludeVowels(boolean includeVowels) {
		this.includeVowels = includeVowels;
	}

	/**
	 * Detect configured segmental relations.
	 * 
	 * @param pm
	 * @return
	 */
	public List<SegmentalRelation> detect(PhoneMap pm) {
		if(detectors.size() == 0) initDefaultDetectors();
		
		final List<SegmentalRelation> relations = new ArrayList<>();
		
		for(int i = 0; i < pm.getAlignmentLength()-1; i++) {
			List<SegmentalRelation> elementRelations = new ArrayList<>();
			for(int j = i+1; j < pm.getAlignmentLength(); j++) {
				final int p1 = i;
				final int p2 = j;

				final IPAElement t1 = pm.getAlignedElements(p1).get(0);
				final IPAElement t2 = pm.getAlignedElements(p2).get(0);
				if(t1 == null && t2 == null) continue;
				boolean isConsonant = 
						(t1 != null ? t1.getFeatureSet().hasFeature("consonant") : t2.getFeatureSet().hasFeature("consonant"));
				
				if(isConsonant && !isIncludeConsonants()) continue;
				if(!isConsonant && !isIncludeVowels()) continue;
				
				if(t2 != null) {
					if( (isConsonant && t2.getFeatureSet().hasFeature("vowel"))
							|| (!isConsonant && t2.getFeatureSet().hasFeature("consonant")) ) continue;
				}
				
				final IPAElement a1 = pm.getAlignedElements(p1).get(1);
				final IPAElement a2 = pm.getAlignedElements(p2).get(1);
				if(a1 == null && a2 == null) continue;
				if(a1 != null) {
					if( (isConsonant && a1.getFeatureSet().hasFeature("vowel")) 
							|| (!isConsonant && a1.getFeatureSet().hasFeature("consonant")) ) continue;
				}
				if(a2 != null) {
					if( (isConsonant && a2.getFeatureSet().hasFeature("vowel")) 
							|| (!isConsonant && a2.getFeatureSet().hasFeature("consonant")) ) continue;
				
				}
				
				for(SegmentalRelationDetector detector:detectors) {
					final Optional<SegmentalRelation> relation = detector.detect(pm, p1, p2);
					if(relation.isPresent())
						elementRelations.add(relation.get());
				}
			}
			
			relations.addAll(elementRelations);
		}
	
		Collections.sort(relations);
		final List<Integer> positions = new ArrayList<>();
		final Iterator<SegmentalRelation> itr = relations.iterator();
		while(itr.hasNext()) {
			final SegmentalRelation relation = itr.next();
			
			if(positions.contains(relation.getPosition1()) || positions.contains(relation.getPosition2())) {
				itr.remove();
			} else {
				positions.add(relation.getPosition1());
				positions.add(relation.getPosition2());
			}
		}
		
		return relations;
	}

}
