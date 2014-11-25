package ca.phon.ipa.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Sort {@link IPAElement}s by feature
 *
 */
public class FeatureComparator implements Comparator<IPAElement> {
	
	private final List<FeatureSet> featureSetOrdering = 
			Collections.synchronizedList(new ArrayList<FeatureSet>());
	
	public static FeatureComparator createPlaceComparator() {
		final FeatureSet[] featureSets = new FeatureSet[7];
		featureSets[0] = FeatureSet.fromArray(new String[]{"Labial"});
		featureSets[1] = FeatureSet.fromArray(new String[]{"Coronal"});
		featureSets[2] = FeatureSet.fromArray(new String[]{"Dorsal"});
		featureSets[3] = FeatureSet.fromArray(new String[]{"Guttural"});
		featureSets[4] = FeatureSet.fromArray(new String[]{"v,Front"});
		featureSets[5] = FeatureSet.fromArray(new String[]{"v,Central"});
		featureSets[6] = FeatureSet.fromArray(new String[]{"v,Back"});
		return new FeatureComparator(featureSets);
	}
	
	public FeatureComparator() {
		super();
	}
	
	public FeatureComparator(FeatureSet ... featureSets) {
		this(Arrays.asList(featureSets));
	}
	
	public FeatureComparator(List<FeatureSet> featureSets) {
		featureSetOrdering.addAll(featureSets);
	}
	
	public void addFeatureSet(FeatureSet fs) {
		featureSetOrdering.add(fs);
	}
	
	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		final int i1 = primaryOrder(o1);
		final int i2 = primaryOrder(o2);
		
		if(i1 == i2) {
			// same group, order by string value
			return o1.toString().compareTo(o2.toString());
		} else {
			return ((Integer)i1).compareTo(i2);
		}
	}
	
	private int primaryOrder(IPAElement ele) {
		final EleVisitor visitor = new EleVisitor();
		visitor.visit(ele);
		
		return visitor.value;
	}
	
	public class EleVisitor extends VisitorAdapter<IPAElement> {

		int value = -1;
		
		@Override
		public void fallbackVisit(IPAElement obj) {
			value = -1;
		}
		
		@Visits
		public void visitPhone(Phone phone) {
			final FeatureSet fs = phone.getBaseFeatures();
			
			for(int i = 0; i < featureSetOrdering.size(); i++) {
				final FeatureSet cmpFs = featureSetOrdering.get(i);
				final FeatureSet intersection = FeatureSet.intersect(cmpFs, fs);
				if(intersection.equals(cmpFs)) {
					value = i;
					break;
				}
			}
		}
		
		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			visitPhone(cp.getFirstPhone());
			if(value == 0)
				visitPhone(cp.getSecondPhone());
		}
		
	}
	
}
