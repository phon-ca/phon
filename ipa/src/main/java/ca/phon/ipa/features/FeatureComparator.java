/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.ipa.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Phone;
import ca.phon.util.PrefHelper;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Sort {@link IPAElement}s by feature.
 *
 */
public class FeatureComparator implements Comparator<IPAElement> {
	
	private final static Logger LOGGER = Logger.getLogger(FeatureComparator.class.getName());
	
	public final static String FEATURE_ORDERING = FeatureComparator.class.getName() + ".featureOrder";
	
	private final static String DEFAULT_FEATURE_ORDERING = "feature_ordering.dat";
	
	private final List<FeatureSet> featureSetOrdering = new ArrayList<>();
	
	private FeatureSet[] excludedFeatureSets = new FeatureSet[0];
	
	
	private static FeatureComparator _default;
	
	public static FeatureComparator defaultComparator() {
		if(_default == null) {
			final FeatureComparator retVal = new FeatureComparator();
			
			final String orderFile = PrefHelper.get(FEATURE_ORDERING, DEFAULT_FEATURE_ORDERING);
			try {
				InputStream is = null;
				if(DEFAULT_FEATURE_ORDERING.equals(orderFile)) {
					is = FeatureComparator.class.getResourceAsStream(DEFAULT_FEATURE_ORDERING);
				} else {
					is = new FileInputStream(new File(orderFile));
				}
				
				final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line = null;
				while((line = in.readLine()) != null) {
					// parse feature set
					line = line.trim();
					if(!line.matches("\\{.+\\}")) continue;
					
					line = line.replace("{", "").replace("}", "");
					String[] features = line.split(",");
					FeatureSet includedFeatures = new FeatureSet();
					FeatureSet excludedFeatures = new FeatureSet();
					for(String feature:features) {
						feature = feature.trim();
						if(feature.startsWith("-")) {
							excludedFeatures.addFeature(feature.substring(1));
						} else {
							includedFeatures.addFeature(feature);
						}
					}
					
					retVal.addFeatureSet(includedFeatures, (excludedFeatures.size() > 0 ? excludedFeatures : null));
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			_default = retVal;
		}
		return _default;
	}
	
	// XXX Unsure about this comparator
	public static FeatureComparator createContinuancyComparator() {
		final FeatureSet[] featureSets = new FeatureSet[3];
		featureSets[0] = FeatureSet.fromArray(new String[]{"Stop"});
		featureSets[1] = FeatureSet.fromArray(new String[]{"Fricative"});
		featureSets[2] = FeatureSet.fromArray(new String[]{"Approximant"});
		return new FeatureComparator(featureSets);
	}
	
	public static FeatureComparator createMannerComparator() {
		final FeatureSet[] featureSets = new FeatureSet[11];
		featureSets[0] = FeatureSet.fromArray(new String[]{"obs, Stop, voiceless"});
		featureSets[1] = FeatureSet.fromArray(new String[]{"obs, Stop, voiced"});
		featureSets[2] = FeatureSet.fromArray(new String[]{"obs, Fricative, voiceless"});
		featureSets[3] = FeatureSet.fromArray(new String[]{"obs, Fricative, voiced"});
		featureSets[4] = FeatureSet.fromArray(new String[]{"obs, Affricate, voiceless"});
		featureSets[5] = FeatureSet.fromArray(new String[]{"obs, Affricate, voiced"});
		featureSets[6] = FeatureSet.fromArray(new String[]{"Nasal"});
		featureSets[7] = FeatureSet.fromArray(new String[]{"Lateral"});
		featureSets[8] = FeatureSet.fromArray(new String[]{"Rhotic"});
		featureSets[9] = FeatureSet.fromArray(new String[]{"Glide"});
		featureSets[10] = FeatureSet.fromArray(new String[]{"Vowel"});
		return new FeatureComparator(featureSets);
	}
	
	public static FeatureComparator createVoicingComparator() {
		final FeatureSet[] featureSets = new FeatureSet[2];
		featureSets[0] = FeatureSet.fromArray(new String[]{"Voiceless"});
		featureSets[1] = FeatureSet.fromArray(new String[]{"Voiced"});
		return new FeatureComparator(featureSets);
	}
	
	public static FeatureComparator createPlaceComparator() {
		final FeatureSet[] featureSets = new FeatureSet[12];
		featureSets[0] = FeatureSet.fromArray(new String[]{"Labial"});
		featureSets[1] = FeatureSet.fromArray(new String[]{"Coronal, Dental"});
		featureSets[2] = FeatureSet.fromArray(new String[]{"Alveolar"});
		featureSets[3] = FeatureSet.fromArray(new String[]{"Alveopalatal"});
		featureSets[4] = FeatureSet.fromArray(new String[]{"Posterior"});
		featureSets[5] = FeatureSet.fromArray(new String[]{"Velar"});
		featureSets[6] = FeatureSet.fromArray(new String[]{"Uvular"});
		featureSets[7] = FeatureSet.fromArray(new String[]{"Pharyngeal"});
		featureSets[8] = FeatureSet.fromArray(new String[]{"Laryngeal"});
		featureSets[9] = FeatureSet.fromArray(new String[]{"v,Front"});
		featureSets[10] = FeatureSet.fromArray(new String[]{"v,Central"});
		featureSets[11] = FeatureSet.fromArray(new String[]{"v,Back"});
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
		excludedFeatureSets = new FeatureSet[featureSets.size()];
	}
	
	public void addFeatureSet(FeatureSet fs, FeatureSet excluded) {
		featureSetOrdering.add(fs);
		excludedFeatureSets = Arrays.copyOf(excludedFeatureSets, featureSetOrdering.size());
		excludedFeatureSets[excludedFeatureSets.length-1] = excluded;
	}
	
	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		final int i1 = primaryOrder(o1);
		final int i2 = primaryOrder(o2);
		
		return ((Integer)i1).compareTo(i2);
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
					
					final FeatureSet exludedFeatures = excludedFeatureSets[i];
					if(exludedFeatures != null) {
						final FeatureSet excludedIntersection = FeatureSet.intersect(exludedFeatures, cmpFs);
						if(excludedIntersection.size() != 0) {
							continue;
						}
					}
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
