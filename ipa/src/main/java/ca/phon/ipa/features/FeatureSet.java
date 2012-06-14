/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
import java.util.Set;
import java.util.logging.Logger;

import ca.phon.util.StringUtils;

/**
 * Class to represent a charater's feature
 * set.
 *
 */
public class FeatureSet 
{
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
	
	/** The collection of diacritics associated with this phone */
//	private final Collection<FeatureSet> diacritics;
	
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
//			if(f.length() == 1) {
//				FeatureSet charFs = FeatureMatrix.getInstance().getFeatureSet(f.charAt(0));
//				fs.addAll(charFs.getFeatures());
//			} else {
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
//			}
		}
		
		return new FeatureSet(fs);
	}
	
	/** Create a new instance of a feature set
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
//		this.diacritics = new ArrayList<FeatureSet>();
	}
	
	public FeatureSet(BitSet featureSet) {
		this.features = featureSet;
		this.ipaChar = '\u0000';
	}
	
	/** Add a new feature to the set */
	public FeatureSet addFeature(String feature) {
//		this.features.add(feature);
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		this.features.or(fs.features);
		return this;
	}
	
	/** Remove a feature from the set */
	public FeatureSet removeFeature(String feature) {
//		this.features.remove(feature);
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		this.features.andNot(fs.features);
		return this;
	}
	
	/** Check for existance of a feature */
	public boolean hasFeature(String feature) {
//		return this.features.contains(feature);
		FeatureSet fs = 
			FeatureMatrix.getInstance().getFeatureSetForFeature(feature);
		return this.features.intersects(fs.features);
	}
	
	/** Returnt the features.  This is not a live list. */
	public Collection<String> getFeatures() {
		Collection<String> retVal = new ArrayList<String>();
//		Set<String> featureSet = FeatureMatrix.getInstance().getFeatures();
//		String allFeatures[] = featureSet.toArray(new String[0]);
//		
		for(int i = this.features.nextSetBit(0);
			i >= 0; i = this.features.nextSetBit(i+1)) {
			retVal.add(FeatureMatrix.getInstance().getFeatureForIndex(i));
		}
//		
		return retVal;
//		return this.features;
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
//		for(Iterator i = this.diacritics.iterator(); i.hasNext();) {
//			retVal += ((FeatureSet)i.next()).getIpaChar();
//		}
		return retVal;
	}
	
	/**
	 * @param ipaChar The ipaChar to set.
	 */
	public void setIpaChar(char ipaChar) {
		this.ipaChar = ipaChar;
	}
	
//	/**
//	 * @param diacriticFS the feature set for a diacritic
//	 */
//	public void addDiacritic(FeatureSet diacriticFS) {
//		this.diacritics.add(diacriticFS);
//	}
//	
//	/**
//	 * @return the list of diacritics
//	 */
//	public Collection getDiacritics() {
//		return this.diacritics;
//	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FeatureSet)) return false;
		
		FeatureSet fs = (FeatureSet)obj;
		
		return this.features.equals(fs.features);
		
//		Collection<String> fsFeatures = fs.getFeatures();
//		Collection<String> myFeatures = this.getFeatures();
//		
//		if(fsFeatures.size() != myFeatures.size()) return false;
//		
//		for(String testFeature:myFeatures) {
//			if(!fs.hasFeature(testFeature))
//				return false;
//		}
//		
//		return true;
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
//		FeatureSet unionFeatures = new FeatureSet();
//		Collection fs1Features = fs1.getFeatures();
//		Collection fs2Features = fs2.getFeatures();
//		
//		for(Iterator i = fs1Features.iterator(); i.hasNext(); )
//			unionFeatures.addFeature(i.next().toString());
//		for(Iterator i = fs2Features.iterator(); i.hasNext(); )
//			unionFeatures.addFeature(i.next().toString());
//		return unionFeatures;
//		FeatureSet retVal = new FeatureSet();
		
//		// the hashset takes care of duplicates
//		retVal.features.addAll(fs1.getFeatures());
//		retVal.features.addAll(fs2.getFeatures());
//		
//		return retVal;
		
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
//		FeatureSet intersectFeatures = new FeatureSet();
//		Collection fs1Features = fs1.getFeatures();
//		Collection fs2Features = fs2.getFeatures();
//		
//		for(Iterator i = fs1Features.iterator(); i.hasNext(); ) {
//			String feature = i.next().toString();
//			if(fs2Features.contains(feature))
//				intersectFeatures.addFeature(feature);
//		}
//		return intersectFeatures;
		
		BitSet bs = 
			(BitSet)fs1.features.clone();
		bs.and(fs2.features);
		
//		if(PhonUtilities.isDebugMode()) {
//			PhonLogger.fine("FS1: " + 
//					fs1.toString());
//			PhonLogger.fine("FS2: " + 
//					fs2.toString());
//			PhonLogger.fine("Intersect: " + 
//					(new FeatureSet(bs)).toString());
//		}
		
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
//		FeatureSet minusFeatures = new FeatureSet();
//		Collection fs1Features = fs1.getFeatures();
//		Collection fs2Features = fs2.getFeatures();
//		
//		for(Iterator i = fs1Features.iterator(); i.hasNext();)
//			minusFeatures.addFeature(i.next().toString());
//		
//		for(Iterator i = fs2Features.iterator(); i.hasNext(); ) {
//			String feature = i.next().toString();
//			if(fs1Features.contains(feature))
//				minusFeatures.removeFeature(feature);
//		}
//		return minusFeatures;
		
		BitSet bs = 
			(BitSet)fs1.features.clone();
		bs.andNot(fs2.features);
		
//		if(PhonUtilities.isDebugMode()) {
//			PhonLogger.fine("FS1: " + 
//					fs1.toString());
//			PhonLogger.fine("FS2: " + 
//					fs2.toString());
//			PhonLogger.fine("Exclusion: " + 
//					(new FeatureSet(bs)).toString());
//		}
		
		return new FeatureSet(bs);
	}
}