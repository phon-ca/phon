/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * Class to represent a charater's feature
 * set.
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
	
	/** The character this feature set represents */
	private char ipaChar;
	
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
						Logger.getLogger(FeatureSet.class.getName()).warning("Unknown feature: " + feature);
					}
				}
				
			} else {
				if(FeatureMatrix.getInstance().getFeature(f.toLowerCase()) != null) {
					fs.add(f);
				} else {
					Logger.getLogger(FeatureSet.class.getName()).warning("Unknown feature: " + f);
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
		this.ipaChar = '\u0000';
	}
	
	public FeatureSet(Set<String> features) {
		this.features = new BitSet();
		this.ipaChar = '\u0000';
		for(String f:features) {
			addFeature(f);
		}
	}
	
	public FeatureSet(BitSet featureSet) {
		this.features = featureSet;
		this.ipaChar = '\u0000';
	}
	
	/** Add a new feature to the set */
	public FeatureSet addFeature(String feature) {
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		this.features.or(fs.features);
		return this;
	}
	
	/** Remove a feature from the set */
	public FeatureSet removeFeature(String feature) {
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

		return retVal;
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
	
	/**
	 * @return Returns the ipaChar.
	 */
	public String getIpaChar() {
		String retVal = new String();
		retVal += ipaChar + "";
		return retVal;
	}
	
	/**
	 * @param ipaChar The ipaChar to set.
	 */
	public void setIpaChar(char ipaChar) {
		this.ipaChar = ipaChar;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FeatureSet)) return false;
		
		FeatureSet fs = (FeatureSet)obj;
		
		return this.features.equals(fs.features);
	}
	
	public FeatureSet union(FeatureSet fs2) {
		FeatureSet fs = FeatureSet.union(this, fs2);
		this.features = fs.features;
		return this;
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
	
	public FeatureSet intersect(FeatureSet fs2) {
		FeatureSet fs = FeatureSet.intersect(this, fs2);
		this.features = fs.features;
		return this;
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
	
	public FeatureSet minus(FeatureSet fs2) {
		FeatureSet fs = FeatureSet.minus(this, fs2);
		this.features = fs.features;
		return this;
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