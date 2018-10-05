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

package ca.phon.ipa.features;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Class to represent a charater's feature set.
 * Features are stored internally as a {@link BitSet}.
 * As of Phon 2.2 objects of this type are immutable after construction.
 *
 */
public class FeatureSet implements Iterable<Feature> {
	/** Sonority Class constants */
	final static int STOP      = 0;
	final static int FRICATIVE = 1;
	final static int NASAL     = 2;
	final static int LATERAL   = 3;
	final static int RHOTIC    = 4;
	final static int GLIDE     = 5;
	final static int VOWEL     = 6;
	
	/** The set of features */
	private BitSet features;
	
	/**
	 * Create a feature set from a single feature.
	 * 
	 * @param featureSet
	 */
	public static FeatureSet singleonFeature(String featureName) {
		Set<String> fs = new HashSet<>();
		Feature fObj = FeatureMatrix.getInstance().getFeature(featureName);
		if(fObj != null)
			fs.add(featureName);
		return new FeatureSet(fs);
	}
	
	/**
	 * Utility method for creating features sets from
	 * an array of values.
	 * 
	 * @param features the list of features to
	 * add to the set.  If the features[x] is a single character,
	 * the full feature set of the character is added.
	 * @return the created feature set
	 */
	public static FeatureSet fromArray(String[] features) {
		Set<String> fs = new HashSet<String>();
		
		for(String f:features) {
			if(f.indexOf(',') >= 0) {
				//seperate and add features
				String[] tokens = f.split(",");
				for(int i = 0; i < tokens.length; i++) {
					String feature = StringUtils.strip(tokens[i]).toLowerCase();
					Feature fObj = FeatureMatrix.getInstance().getFeature(feature);
					if(fObj != null) {
						fs.add(feature);
					} else {
						org.apache.logging.log4j.LogManager.getLogger(FeatureSet.class.getName()).warn("Unknown feature: " + feature);
					}
				}
				
			} else {
				if(FeatureMatrix.getInstance().getFeature(f.toLowerCase()) != null) {
					fs.add(f);
				} else {
					org.apache.logging.log4j.LogManager.getLogger(FeatureSet.class.getName()).warn("Unknown feature: " + f);
				}
			}
		}
		
		return new FeatureSet(fs);
	}
	
	/** 
	 * Create a new instance of a feature set
	 * 
	 */
	public FeatureSet() {
		this.features = new BitSet();
	}
	
	public FeatureSet(Set<String> features) {
		this.features = new BitSet();
		for(String f:features) {
			addFeature(f);
		}
	}
	
	public FeatureSet(BitSet featureSet) {
		this.features = featureSet;
	}
	
	/** Add a new feature to the set */
	private FeatureSet addFeature(String feature) {
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		this.features.or(fs.features);
		return this;
	}
	
	/** Remove a feature from the set */
	private FeatureSet removeFeature(String feature) {
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		this.features.andNot(fs.features);
		return this;
	}
	
	/** Check for existance of a feature */
	public boolean hasFeature(String feature) {
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		return this.features.intersects(fs.features);
	}
	
	/** Returnt the features.  This is not a live list. */
	public Collection<String> getFeatures() {
		Collection<String> retVal = new ArrayList<String>();

		for(int i = this.features.nextSetBit(0);
			i >= 0; i = this.features.nextSetBit(i+1)) {
			retVal.add(FeatureMatrix.getInstance().getFeatureForIndex(i));
		}

		return Collections.unmodifiableCollection(retVal);
	}
	
	/**
	 * Returns the number of features in the set.
	 * @return the cardinality of the set
	 */
	public int size() {
		return this.features.cardinality();
	}
	
	public int sonority(){
		if(hasFeature("Stop")) return STOP;
		if(hasFeature("Fricative")) return FRICATIVE;
		if(hasFeature("Nasal")) return NASAL;
		if(hasFeature("Lateral")) return LATERAL;
		if(hasFeature("Rhotic")) return RHOTIC;
		if(hasFeature("Glide")) return GLIDE;
		if(hasFeature("Vowel")) return VOWEL;
		else return -1;
	}
	
	/** A string representation of the feature set */
	@Override
	public String toString() {
		return getFeatures().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FeatureSet)) return false;
		
		FeatureSet fs = (FeatureSet)obj;
		
		return this.features.equals(fs.features);
	}
	
	/**
	 * Produces a new FeatureSet based on the set-union
	 * of the two given FeatureSets
	 * 
	 * @param fs1
	 * @param fs2
	 *
	 * @return fs1 UNION fs2
	 */
	public static FeatureSet union(FeatureSet fs1, FeatureSet fs2) {
		BitSet bs = 
			(BitSet)fs1.features.clone();
		bs.or(fs2.features);
		
		return new FeatureSet(bs);
	}
	
	public boolean intersects(FeatureSet fs2) {
		return FeatureSet.intersects(this, fs2);
	}
	
	/**
	 * Do the features sets intersect.
	 * 
	 * @param fs1
	 * @param fs2
	 * @return true if the two given featuresets
	 * have any common features.
	 */
	public static boolean intersects(FeatureSet fs1, FeatureSet fs2) {
		return fs1.features.intersects(fs2.features);
	}
	
	/**
	 * Produces a new FeatureSet based on the set-intersection
	 * of the two given FeatureSets.
	 * 
	 * @param fs1
	 * @param fs2
	 *
	 * @return fs1 INTERSECT fs2
	 */
	public static FeatureSet intersect(FeatureSet fs1, FeatureSet fs2) {
		BitSet bs = 
			(BitSet)fs1.features.clone();
		bs.and(fs2.features);
		return new FeatureSet(bs);
	}
	
	/**
	 * Produces a new FeatureSet based on the set-subtraction
	 * of the given feature sets.
	 * 
	 * @param fs1
	 * @param fs2
	 * 
	 * @return fs1 MINUS fs2
	 * 
	 *
	 */
	public static FeatureSet minus(FeatureSet fs1, FeatureSet fs2) {
		BitSet bs = 
			(BitSet)fs1.features.clone();
		bs.andNot(fs2.features);
		return new FeatureSet(bs);
	}
	
	/**
	 * Return the set of manner features for this feature set.
	 * 
	 * @return manner features
	 */
	public FeatureSet getManner() {
		FeatureSet retVal = new FeatureSet();
		for(Feature f:this) {
			if(f.getPrimaryFamily() == FeatureFamily.MANNER)
				retVal.addFeature(f.getName());
		}
		return retVal;
	}
	
	public FeatureSet getPlace() {
		FeatureSet retVal = new FeatureSet();
		for(Feature f:this) {
			if(f.getPrimaryFamily() == FeatureFamily.PLACE)
				retVal.addFeature(f.getName());
		}
		return retVal;
	}
	
	public FeatureSet getVoicing() {
		FeatureSet retVal = new FeatureSet();
		for(Feature f:this) {
			if(f.getPrimaryFamily() == FeatureFamily.VOICING)
				retVal.addFeature(f.getName());
		}
		return retVal;
	}
	
	@Override
	public Iterator<Feature> iterator() {
		return new FeatureIterator();
	}
	
	private class FeatureIterator implements Iterator<Feature> {
		
		private final Iterator<String> strItr = getFeatures().iterator();

		@Override
		public boolean hasNext() {
			return strItr.hasNext();
		}

		@Override
		public Feature next() {
			final String nextFeature = strItr.next();
			return FeatureMatrix.getInstance().getFeature(nextFeature);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}