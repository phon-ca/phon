package ca.phon.ipa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ca.phon.ipa.features.FeatureSet;

/**
 * Compare two IPAElements based on features.
 */
public class FeatureComparator implements Comparator<IPAElement> {
	
	private final List<FeatureSet> featureSets = new ArrayList<FeatureSet>();
	
	public static FeatureComparator createPlaceComparator() {
		return new FeatureComparator(
				FeatureSet.fromArray(new String[]{"Labial"}),
				FeatureSet.fromArray(new String[]{"Coronal"}),
				FeatureSet.fromArray(new String[]{"Dorsal"}),
				FeatureSet.fromArray(new String[]{"Glottal"}));
	}
	
	public static FeatureComparator createVoicingComparator() {
		return new FeatureComparator(
				FeatureSet.fromArray(new String[]{"Voiceless"}),
				FeatureSet.fromArray(new String[]{"Voiced"}));
	}
	
	public static FeatureComparator createMannerComparator() {
		return new FeatureComparator(
				FeatureSet.fromArray(new String[]{"Stop"}),
				FeatureSet.fromArray(new String[]{"Fricative"}),
				FeatureSet.fromArray(new String[]{"Affricate"}),
				FeatureSet.fromArray(new String[]{"Nasal"}),
				FeatureSet.fromArray(new String[]{"Lateral"}),
				FeatureSet.fromArray(new String[]{"Rhotic"}),
				FeatureSet.fromArray(new String[]{"Glide"}),
				FeatureSet.fromArray(new String[]{"Vowel"}));
	}
	
	public static FeatureComparator createContinuancyComparator() {
		return new FeatureComparator(
				FeatureSet.fromArray(new String[]{"Stop"}),
				FeatureSet.fromArray(new String[]{"Fricative"}),
				FeatureSet.fromArray(new String[]{"Approximant"}));
	}
	
	public FeatureComparator() {
		super();
	}
	
	public FeatureComparator(FeatureSet ... sets) {
		this(Arrays.asList(sets));
	}
	
	public FeatureComparator(List<FeatureSet> sets) {
		super();
		featureSets.addAll(sets);
	}
	
	/**
	 * Live list of feature sets.
	 * 
	 * @return
	 */
	public List<FeatureSet> getFeatureSets() {
		return this.featureSets;
	}
	
	private int featureSetIndex(IPAElement ele) {
		int retVal = -1;
		
		final FeatureSet fs = ele.getFeatureSet();
		for(int i = 0; i < featureSets.size(); i++) {
			final FeatureSet testFs = featureSets.get(i);
			if(fs.intersect(testFs).equals(testFs)) {
				retVal = i;
				break;
			}
		}
		
		return retVal;
	}

	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		final Integer o1Idx = featureSetIndex(o1);
		final Integer o2Idx = featureSetIndex(o2);
		
		if(o1Idx == o2Idx) {
			return o1.getText().compareTo(o2.getText());
		} else {
			return o1Idx.compareTo(o2Idx);
		}
	}

}
