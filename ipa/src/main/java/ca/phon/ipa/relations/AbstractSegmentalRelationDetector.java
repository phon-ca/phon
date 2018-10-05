/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.PhoneticProfile;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.relations.SegmentalRelation.Relation;

public abstract class AbstractSegmentalRelationDetector implements SegmentalRelationDetector {
	
	/* Consonant dimensions */
	private boolean includePlace = true;

	private boolean includeManner = true;
	
	private boolean includeVoicing = true;
	
	/* Vowel dimensions */
	private boolean includeHeight = true;
	
	private boolean includeBackness = true;
	
	private boolean includeTenseness = true;
	
	private boolean includeRounding = true;
	
	private final Relation relation;
	
	/*
	 * Is the relation reversible (progressive or regressive)
	 * default is true
	 */
	private boolean reversible = true;
	
	/*
	 * Locality restraints
	 */
	private boolean localAllowed = true;
	
	private boolean nonlocalAllowed = true;
	
	public AbstractSegmentalRelationDetector(SegmentalRelation.Relation relation) {
		super();
		this.relation = relation;
	}
	
	public AbstractSegmentalRelationDetector(SegmentalRelation.Relation relation, boolean includePlace, boolean includeManner, boolean includeVoicing,
			boolean includeHeight, boolean includeBackness, boolean includeTenseness, boolean includeRounding) {
		super();
		this.relation = relation;
		this.includePlace = includePlace;
		this.includeManner = includeManner;
		this.includeVoicing = includeVoicing;
		this.includeHeight = includeHeight;
		this.includeBackness = includeBackness;
		this.includeTenseness = includeTenseness;
		this.includeRounding = includeRounding;
	}
	
	public AbstractSegmentalRelationDetector(SegmentalRelation.Relation relation, boolean reversible, 
			boolean localAllowed, boolean nonlocalAllowed) {
		super();
		this.relation = relation;
		this.reversible = reversible;
		this.localAllowed = localAllowed;
		this.nonlocalAllowed = nonlocalAllowed;
	}
	
	public Relation getRelation() {
		return this.relation;
	}

	public boolean isIncludePlace() {
		return includePlace;
	}

	public void setIncludePlace(boolean includePlace) {
		this.includePlace = includePlace;
	}

	public boolean isIncludeManner() {
		return includeManner;
	}

	public void setIncludeManner(boolean includeManner) {
		this.includeManner = includeManner;
	}

	public boolean isIncludeVoicing() {
		return includeVoicing;
	}

	public void setIncludeVoicing(boolean includeVoicing) {
		this.includeVoicing = includeVoicing;
	}

	public boolean isIncludeHeight() {
		return includeHeight;
	}

	public void setIncludeHeight(boolean includeHeight) {
		this.includeHeight = includeHeight;
	}

	public boolean isIncludeBackness() {
		return includeBackness;
	}

	public void setIncludeBackness(boolean includeBackness) {
		this.includeBackness = includeBackness;
	}

	public boolean isIncludeTenseness() {
		return includeTenseness;
	}

	public void setIncludeTenseness(boolean includeTenseness) {
		this.includeTenseness = includeTenseness;
	}

	public boolean isIncludeRounding() {
		return includeRounding;
	}

	public void setIncludeRounding(boolean includeRounding) {
		this.includeRounding = includeRounding;
	}
	
	public boolean isReversible() {
		return reversible;
	}

	public void setReversible(boolean reversible) {
		this.reversible = reversible;
	}

	public boolean isLocalAllowed() {
		return localAllowed;
	}

	public void setLocalAllowed(boolean localAllowed) {
		this.localAllowed = localAllowed;
	}

	public boolean isNonlocalAllowed() {
		return nonlocalAllowed;
	}

	public void setNonlocalAllowed(boolean nonlocalAllowed) {
		this.nonlocalAllowed = nonlocalAllowed;
	}

	@Override
	public Optional<SegmentalRelation> detect(PhoneMap pm, int p1, int p2) {
		final IPAElement tEle = (pm.getAlignedElements(p1).get(0) != null ?
				pm.getAlignedElements(p1).get(0) : pm.getAlignedElements(p2).get(0));
		
		Optional<SegmentalRelation> retVal = Optional.empty();
		if(tEle.getFeatureSet().hasFeature("consonant")) {
			retVal = detectConsonantRelation(pm, p1, p2);
			if(isReversible() && !retVal.isPresent()) {
				retVal = detectConsonantRelation(pm, p2, p1);
			}
		} else {
			retVal = detectVowelRelation(pm, p1, p2);
			if(isReversible() && !retVal.isPresent()) {
				retVal = detectVowelRelation(pm, p2, p1);
			}
		}
		return retVal;
	}

	public Optional<SegmentalRelation> detectConsonantRelation(PhoneMap pm, int p1, int p2) {
		List<PhoneDimension> dimensions = new ArrayList<>();
		if(isIncludePlace()) dimensions.add(PhoneDimension.PLACE);
		if(isIncludeManner()) dimensions.add(PhoneDimension.MANNER);
		if(isIncludeVoicing()) dimensions.add(PhoneDimension.VOICING);
		
		return detectRelation(pm, p1, p2, dimensions);
	}
	
	public Optional<SegmentalRelation> detectVowelRelation(PhoneMap pm, int p1, int p2) {
		List<PhoneDimension> dimensions = new ArrayList<>();
		if(isIncludeHeight()) dimensions.add(PhoneDimension.HEIGHT);
		if(isIncludeBackness()) dimensions.add(PhoneDimension.BACKNESS);
		if(isIncludeTenseness()) dimensions.add(PhoneDimension.TENSENESS);
		if(isIncludeRounding()) dimensions.add(PhoneDimension.ROUNDING);
		
		return detectRelation(pm, p1, p2, dimensions);
	}
	
	protected boolean checkPositions(PhoneMap pm, int p1, int p2) {
		boolean isLocal = (Math.abs(p2-p1) == 1);
		if(isLocal && !isLocalAllowed()) return false;
		
		if(!isLocal && !isNonlocalAllowed()) return false;
		
		return true;
	}
	
	public Optional<SegmentalRelation> detectRelation(PhoneMap pm, int p1, int p2, List<PhoneDimension> dimensions) {
		if(!checkPositions(pm, p1, p2)) return Optional.empty();
		
		PhoneticProfile t1Profile = 
				(p1 >= 0 ? new PhoneticProfile(pm.getTopAlignmentElements().get(p1)) : new PhoneticProfile());
		PhoneticProfile t2Profile = 
				(p2 >= 0 ? new PhoneticProfile(pm.getTopAlignmentElements().get(p2)) : new PhoneticProfile());
		
		PhoneticProfile a1Profile = 
				(p1 >= 0 ? new PhoneticProfile(pm.getBottomAlignmentElements().get(p1)) : new PhoneticProfile());
		PhoneticProfile a2Profile = 
				(p2 >= 0 ? new PhoneticProfile(pm.getBottomAlignmentElements().get(p2)) : new PhoneticProfile());
		
		PhoneticProfile profile1 = new PhoneticProfile();
		PhoneticProfile profile2 = new PhoneticProfile();
		
		boolean hasHarmony = false;
		for(PhoneDimension dimension:dimensions) {
			hasHarmony |= 
					checkRelation(dimension, profile1, profile2, t1Profile, t2Profile, a1Profile, a2Profile);
		}
		
		if(hasHarmony) {
			final SegmentalRelation relation = 
					new SegmentalRelation(getRelation(), pm, p1, p2, profile1, profile2);
			return Optional.of(relation);
		} else {
			return Optional.empty();
		}
	}
	
	protected abstract boolean checkRelation(PhoneDimension dimension, 
			/*out*/ PhoneticProfile profile1, /*out*/ PhoneticProfile profile2,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile,
			PhoneticProfile a1Profile, PhoneticProfile a2Profile);
	
}
