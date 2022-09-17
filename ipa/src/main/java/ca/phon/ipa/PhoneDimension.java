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
package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

import java.util.*;

/**
 * Phonetic dimensions
 *
 */
public enum PhoneDimension {
	PLACE(2, new String[] {
			"labial,coronal,dorsal,guttural",
			"labiodental,bilabial,anterior,posterior,interdental,alveolar,alveopalatal,palatal,retroflex,velar,uvular,pharyngeal,laryngeal,distributed,grooved"}
		),
	MANNER(2, new String[]{
			"obstruent,approximant,consonant,vowel",
			"stop,fricative,affricate,oral,nasal,sonorant,lateral,rhotic,flap,trill,glide"}
		),
	VOICING(1, new String[]{ "voiced,voiceless,aspirated,plain" }),

	/* Vowels */
	HEIGHT(3, new String[]{ "high,mid,low" }),
	BACKNESS(2, new String[]{ "front,central,back" }),
	TENSENESS(2, new String[]{ "tense,lax" }),
	ROUNDING(1, new String[]{ "rounded,unrounded" });

	private int weight;

	private List<FeatureSet> featureSets;

	private PhoneDimension(int weight, String[] featuresLists) {
		this.weight = weight;

		this.featureSets = new ArrayList<>();
		for(String featureList:featuresLists)
			featureSets.add(FeatureSet.fromArray(featureList.split(",")));
	}

	public int getWeight() {
		return this.weight;
	}

	public FeatureSet getFeatures() {
		FeatureSet retVal = new FeatureSet();
		for(FeatureSet featureSet:featureSets) {
			retVal = FeatureSet.union(retVal, featureSet);
		}
		return retVal;
	}

	public FeatureSet getTerminalFeatures() {
		return featureSets.stream().findFirst().orElse(null);
	}

	@Override
	public String toString() {
		final String name = super.toString();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}

}
