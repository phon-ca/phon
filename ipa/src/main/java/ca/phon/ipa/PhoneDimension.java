package ca.phon.ipa;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.features.FeatureSet;

/**
 * Phonetic dimensions
 *
 */
public enum PhoneDimension {
	PLACE(2, new String[] {
			"labial,coronal,dorsal,guttural",
			"labiodental,bilabial,anterior,posterior",
			"interdental,alveolar,alveopalatal,palatal,retroflex,velar,uvular,pharyngeal,laryngeal"}
		),
	MANNER(2, new String[]{
			"obstruent,approximant,consonant,vowel",
			"stop,fricative,affricate,oral,nasal,sonorant,lateral,rhotic,flap,trill,glide"}
		),
	VOICING(1, new String[]{ "voiced,voiceless,aspirated,plain"}),

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
