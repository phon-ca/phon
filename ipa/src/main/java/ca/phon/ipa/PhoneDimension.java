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
 * A single dimension in the PhoneticProfile of a Phone.  For consonants, these
 * are voicing, place, and manner.  For vowels, these are height, backness,
 * and roundness.  Each dimension has a weight used for alignment as well as a
 * set of primary and a set of terminal features.
 *
 */
public enum PhoneDimension {
	PLACE(2, new String[] {
            "labial, coronal, dorsal, lingual, anterior, posterior, guttural",
            "bilabial, labiodental, interdental, alveolar, alveopalatal, retroflex, palatal, velar, uvular, pharyngeal, laryngeal, epiglottal, dental, apical, laminal, distributed, grooved, subapical, velopharyngeal"}
		),
	MANNER(2, new String[]{
            "obstruent, nasal, liquid, glide, approximant, continuant, sonorant",
            "stop, affricate, fricative, nasal, oral, lateral, rhotic, click, implosive, flap, trill, ejective, prenasalized, strident, quasiresonant, semiresonant, raspberry, transition, narealfricative, percussive"}
		),
	VOICING(1, new String[]{ "voiced,voiceless", "aspirated, plain, unreleased, weaklyaspirated, unaspirated" }),

	/* Vowels */
	HEIGHT(3, new String[]{ "high,mid,low" }),
	BACKNESS(2, new String[]{ "front,central,back" }),
	TENSENESS(2, new String[]{ "tense,lax" }),
	ROUNDING(1, new String[]{ "rounded,unrounded" });

	private final int weight;

	private final List<FeatureSet> featureSets;

	PhoneDimension(int weight, String[] featuresLists) {
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

	public FeatureSet getPrimaryFeatures() {
		return !featureSets.isEmpty() ? featureSets.getFirst() : new FeatureSet();
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
