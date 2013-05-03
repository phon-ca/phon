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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import au.com.bytecode.opencsv.CSVReader;
import ca.phon.featureset.xml.FeatureMatrixType;
import ca.phon.featureset.xml.FeatureSetType;
import ca.phon.featureset.xml.FeatureType;
import ca.phon.featureset.xml.ObjectFactory;
import ca.phon.util.StringUtils;

/**
 * Holds all defined feature set for IPA characters. This information is held in
 * the data/features.xml file.
 * 
 */
public class FeatureMatrix {

	/** The root of the feature matrix. */
	// private XMLFeatureMatrix matrix;
	/** The singleton instance */
	private static FeatureMatrix instance;

	/** Each thread can have it's own feature matrix assigned. */
	private static Map<Thread, FeatureMatrix> _fmMap = Collections
			.synchronizedMap(new LinkedHashMap<Thread, FeatureMatrix>());

	/** The default data file */
	private final static String DATA_FILE = "features.xml";
	
	/** The table of feature sets */
	private LinkedHashMap<Character, FeatureSet> featureSets;

	/** 
	 * Feature name maps for featureData index 
	 * Maps include synonyms for features
	 */
	private LinkedHashMap<String, Integer> featureNameHash;
	private Feature[] featureData;

	private int numberOfFeatures = 0;

	/** Returns the shared instance of the FeatureMatrix */
	public static synchronized FeatureMatrix getInstance() {
		FeatureMatrix retVal = null;
		if (_fmMap.get(Thread.currentThread()) != null) {
			retVal = _fmMap.get(Thread.currentThread());
		} else {
			if (instance == null) {
//				URL defURL = 
//					ClassLoader.getSystemResource(CP_DATA_FILE);
//				if(defURL != null) {
					instance = new FeatureMatrix(
							FeatureMatrix.class.getResourceAsStream(DATA_FILE));
//				} else {
//					instance = new FeatureMatrix(DATA_FILE);
//				}
			}
			retVal = instance;
		}
		return retVal;
	}

	/**
	 * Make the given feature matrix the current for the current thread.
	 * 
	 * @param fm
	 * @return the previous assigned feature matrix for the thread
	 */
	public static synchronized FeatureMatrix makeCurrentInThread(
			FeatureMatrix fm) {
		FeatureMatrix retVal = _fmMap.get(Thread.currentThread());
		if (retVal == null) {
			if (instance == null) {
				instance = new FeatureMatrix(
						FeatureMatrix.class.getResourceAsStream(DATA_FILE));
			}
			retVal = instance;
		}
		_fmMap.put(Thread.currentThread(), fm);
		return retVal;
	}

	/** Create a new instance of the FeatureMatrix */
	public FeatureMatrix(String fmFile) {
		// create the matrix
		if(fmFile.endsWith(".xml")) {
			try {
				buildFromXML(fmFile);
			} catch (IOException e) {
				Logger.getLogger(getClass().getName()).severe(e.toString());
			}
		} else if(fmFile.endsWith(".csv")) {
			try {
				buildFromCSV(fmFile);
			} catch (IOException e) {
				Logger.getLogger(getClass().getName()).severe(e.toString());
			}
		} else {
			throw new IllegalArgumentException("Feature matrix must be in XML or CSV format.");
		}
	}
	
	public FeatureMatrix(InputStream stream) {
		try {
			buildFromXML(stream);
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).severe(e.toString());
			e.printStackTrace();
		}
	}
	
	private void buildFromXML(String fmFile)
		throws IOException {
		FileInputStream stream;
//		try {
			stream = new FileInputStream(fmFile);
			buildFromXML(stream);
//		} catch (IOException e) {
//			Logger.getLogger(getClass().getName()).warning(e.toString());
//		}
	}
	
	/**
	 * Creates the feature matrix from an xml file.
	 * 
	 */
	private void buildFromXML(InputStream stream)
		throws IOException {
		try {
			// parse the file
			JAXBContext jaxbContext = JAXBContext
					.newInstance("ca.phon.featureset.xml");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			JAXBElement<FeatureMatrixType> featureMatrixEle = 
				(JAXBElement<FeatureMatrixType>)unmarshaller.unmarshal(stream);
			FeatureMatrixType matrix = featureMatrixEle.getValue();

			List<FeatureType> featureList = matrix.getFeature();

			// read in features and their families
			// use LinkedHashMap to retain iteration order
			featureNameHash = new LinkedHashMap<String, Integer>();
//			reverseHash = new LinkedHashMap<Integer, String>();
			featureData = new Feature[featureList.size()];

			for (int featureIndex = 0; featureIndex < featureList.size(); featureIndex++) {
				// for(XMLFeatureType feature: featureList){
				FeatureType feature = featureList.get(featureIndex);
				String currentFeature = feature.getName().toLowerCase();

				Feature fd = new Feature(currentFeature);
				if(feature.getPrimaryFamily() != null)
					fd.setPrimaryFamily(FeatureFamily.fromValue(feature.getPrimaryFamily().value()));
				if(feature.getSecondaryFamily() != null)
					fd.setSecondaryFamily(FeatureFamily.fromValue(feature.getSecondaryFamily().value()));
				
				BitSet bitSet = new BitSet(featureList.size());
				bitSet.clear();
				bitSet.set(featureIndex, true);
				fd.setFeatureSet(new FeatureSet(bitSet));
				
				fd.setSynonyms(feature.getSynonym().toArray(new String[0]));
				
				featureNameHash.put(currentFeature, featureIndex);
				for(String synonym:feature.getSynonym())
					featureNameHash.put(synonym.toLowerCase(), featureIndex);
				featureData[featureIndex] = fd;
				// possibleFeatures.put(currentFeature, fd);
			}
			numberOfFeatures = featureList.size();

			// read in feature descriptions and save them
			// in a hashtable
			featureSets = new LinkedHashMap<Character, FeatureSet>();
			List<FeatureSetType> sets = matrix.getFeatureSet();
			for (FeatureSetType set : sets) {
				BitSet bs = new BitSet(numberOfFeatures);
				Character theChar = set.getChar().charAt(0);
				
				for(Object fsObj:set.getValue()) {
					if(!(fsObj instanceof FeatureType)) {
						continue;
					}
					
					FeatureType feature = (FeatureType)fsObj;
					String fName = feature.getName().toLowerCase();
					int fIdx = featureNameHash.get(fName);
					if(fIdx >= 0)
						bs.set(fIdx, true);
				}

				FeatureSet fs = new FeatureSet(bs);
				fs.setIpaChar(theChar);
				featureSets.put(theChar, fs);
			}
		} catch (JAXBException ex) {
			throw new IOException(ex.toString());
		}
	}
	
	/**
	 * Create the feature matrix from a CSV file.
	 */
	private void buildFromCSV(String fmFile) 
		throws IOException {
//		CSVReader reader = new CSVReader(new FileReader(fmFile));
//		String[] colLine = reader.readNext();
//		
//		// first two cols are char and unicode value
//		int sFeatureIdx = 2;
//		featureNameHash = new LinkedHashMap<String, Integer>();
//		reverseHash = new LinkedHashMap<Integer, String>();
//		featureData = new FeatureDescription[colLine.length-2];
//		numberOfFeatures = colLine.length-2;
//		
//		// create list of features
//		for(int i = sFeatureIdx; i < colLine.length; i++) {
//			int featureIndex = i-2;
//			String currentFeature = colLine[i];
//			
//			FeatureDescription fd = new FeatureDescription();
//			fd.featureName = currentFeature;
//			fd.primary = "";
//			fd.secondary = "";
//			BitSet bs = new BitSet(numberOfFeatures);
//			bs.clear();
//			bs.set(featureIndex, true);
//			fd.fs = new FeatureSet(bs);
//			
//			featureNameHash.put(currentFeature, featureIndex);
//			reverseHash.put(featureIndex, currentFeature);
//			featureData[featureIndex] = fd;
//		}
//		
//		featureSets = new LinkedHashMap<Character, FeatureSet>();
//		// create feature sets from remainder rows
//		String[] line = null;
//		while((line = reader.readNext()) != null) {
//			String unicodeValue = line[1];
//			Integer charVal = Integer.decode(unicodeValue);
//			Character currentChar = new Character((char)charVal.byteValue());
//			
//			BitSet bs = new BitSet(numberOfFeatures);
//			bs.clear();
//			
//			for(int i = sFeatureIdx; i < line.length; i++) {
//				String featureIdc = line[i];
//				if(StringUtils.strip(featureIdc).equals("+")) {
//					bs.set(i-2, true);
//				}
//			}
//			
//			FeatureSet fs = new FeatureSet(bs);
//			fs.setIpaChar(currentChar.charValue());
//			featureSets.put(currentChar, fs);
//		}
//		
//		reader.close();
	}

	/**
	 * Returns the number of features.
	 */
	public int getNumberOfFeatures() {
		return numberOfFeatures;
	}

	/**
	 * Return the feature set described by the given ipaChar, or null if
	 * nonexistent.
	 * 
	 * @param ipaChar
	 *            IPA character describing the desired feature set
	 * @return the desired feature set
	 */
	public FeatureSet getFeatureSet(char ipaChar) {
		FeatureSet fs = featureSets.get(ipaChar);
		if (fs == null) {
			fs = new FeatureSet();
			fs.setIpaChar(ipaChar);
		}
		return fs;
	}

	/**
	 * Remove the feature set from the matrix if it exists, do nothing
	 * otherwise.
	 * 
	 * @param ipaChar
	 *            IPA character representing the feature set to remove
	 */
	public void removeFeatureSet(char ipaChar) {
		if (featureSets.keySet().contains(ipaChar)) {
			featureSets.remove(ipaChar);
		}
	}

	/**
	 * Set the given feature set for the given IPA character. If that character
	 * already has feature set defined, replace that set with the given feature
	 * set. Any given feature or feature set that doesn't exist already is
	 * created.
	 * 
	 * @param ipaChar
	 *            IPA character to put the feature set with
	 * @param fs
	 *            feature set to put in
	 */
	public void putFeatureSet(char ipaChar, FeatureSet newFeatureSet) {
		featureSets.put(ipaChar, newFeatureSet);
	}

	/**
	 * Returns a hash set of the names of all the features in the matrix as
	 * strings. If there are no features, returns an empty hash set.
	 * 
	 * @return a hash set of the feature names
	 */
	public Set<String> getFeatures() {
		Set<String> retVal = new HashSet<String>();
		
		for(Feature f:featureData) {
			retVal.add(f.getName());
		}
		
		return retVal;
	}
	
	/**
	 * Returns the Feature object for the given
	 * feature name (or synonym.)
	 * 
	 * @param featureName
	 * @return the Feature or null if not found
	 */
	public Feature getFeature(String featureName) {
		Feature retVal = null;
		
		String fName = featureName.toLowerCase();
		Integer fIdx = featureNameHash.get(fName);
		if(fIdx != null && fIdx >= 0 && fIdx < featureData.length) {
			retVal = featureData[fIdx];
		}
		
		return retVal;
	}

	/**
	 * Returns a hash map with all of the feature sets in the matrix as values.
	 * Each feature set has its character as its matching key. If not feature
	 * sets exist, returns an empty hash map.
	 * 
	 * @return a hash map of characters to feature sets
	 */
	public Map<Character, FeatureSet> getFeatureSets() {
		return featureSets;
	}

	/**
	 * Returns the feature set for the name feature.
	 * 
	 * @param feature
	 *            the feature to look up
	 * @return the featureset for the give feature or an empty set if the
	 *         feature was not found
	 */
	public FeatureSet getFeatureSetForFeature(String feature) {
		BitSet fs = new BitSet(numberOfFeatures);
		fs.clear();
		FeatureSet retVal = new FeatureSet(fs);

		Integer fIdx = featureNameHash.get(feature.toLowerCase());
		if (fIdx != null && fIdx >= 0) {
			retVal = featureData[fIdx].getFeatureSet();
		}
		return retVal;
	}

	/**
	 * Returns a collection of all characters that have the given feature.
	 * 
	 * @param featureName
	 *            the name of the feature to search by
	 * @return collection of all characters that match featureName
	 */
	public Collection<Character> getCharactersWithFeature(String featureName) {
		ArrayList<Character> retVal = new ArrayList<Character>();

		for (Character ch : featureSets.keySet()) {
			FeatureSet fs = featureSets.get(ch);
			if (fs != null && fs.hasFeature(featureName))
				retVal.add(ch);
		}

		return retVal;
	}
	
	/**
	 * Returns the character set supported by this feature matrix
	 * 
	 * @return set of characters
	 */
	public Set<Character> getCharacterSet() {
		return this.featureSets.keySet();
	}

	/**
	 * Returns the name of the primary family for the given feature. null if the
	 * feature does not have a primary family.
	 * 
	 * @param featureName
	 *            name of feature
	 * @return name of feature's primary family
	 */
	public String getFeaturePrimaryFamily(String featureName) {
		String retVal = "";

		Integer fIdx = featureNameHash.get(featureName);
		if (fIdx != null && fIdx >= 0) {
			Feature fd = featureData[fIdx];
			if (fd != null)
				retVal = fd.getPrimaryFamily().value();
		}

		return retVal;
	}

	/**
	 * Returns the feature name for the given index.
	 */
	public String getFeatureForIndex(int idx) {
		return featureData[idx].getName();
	}

	/**
	 * Sets the primary family for the given feature. If family doesn't exist,
	 * it is created. Returns true if successful, false if not. Put will be
	 * unsuccessful if feature doesn't exist.
	 * 
	 * @param featureName
	 *            name of feature
	 * @param familyName
	 *            name of primary family to put with feature
	 * @return true if successful, false if not
	 */
	public boolean putFeaturePrimaryFamily(String featureName, String familyName) {
		boolean retVal = false;
		Integer fIdx = featureNameHash.get(featureName);
		if (fIdx != null && fIdx >= 0) {
			Feature fd = featureData[fIdx];
			if (fd != null) {
				fd.setPrimaryFamily(FeatureFamily.fromValue(familyName.toLowerCase()));
				retVal = true;
			}
		}
		return retVal;
	}

	/**
	 * Returns the secondary families for the given feature. Null if the feature
	 * does not have any secondary families.
	 * 
	 * @param featureName
	 *            name of the feature
	 * @return name of the feature's secondary family
	 */
	public String getFeatureSecondaryFamily(String featureName) {
		String retVal = "";

		Integer fIdx = featureNameHash.get(featureName);
		if (fIdx != null && fIdx >= 0) {
			Feature fd = featureData[fIdx];
			if (fd != null)
				retVal = fd.getSecondaryFamily().value();
		}

		return retVal;
	}

	/**
	 * Sets the secondary family for the given feature. If family doesn't exist,
	 * it is created. Returns true if successful, false if not. Put will be
	 * unsuccessful if feature doesn't exist.
	 * 
	 * @param featureName
	 *            name of feature
	 * @param familyName
	 *            name of secondary family to put with feature
	 * @return true if successful, false if not
	 */
	public boolean putFeatureSecondaryFamily(String featureName,
			String familyName) {
		boolean retVal = false;
		Integer fIdx = featureNameHash.get(featureName);
		if (fIdx != null && fIdx >= 0) {
			Feature fd = featureData[fIdx];
			if (fd != null) {
				fd.setSecondaryFamily(FeatureFamily.fromValue(familyName.toLowerCase()));
				retVal = true;
			}
		}
		return retVal;
	}

	public Collection<String> getFeaturesWithPrimaryFamily(String familyName) {
		ArrayList<String> result = new ArrayList<String>();
		for (String feature : getFeatures()) {
			Integer fIdx = featureNameHash.get(feature);
			if (fIdx != null && fIdx >= 0) {
				Feature fd = featureData[fIdx];
				FeatureFamily family = fd.getPrimaryFamily();
				if (family != null && family == FeatureFamily.fromValue(familyName.toLowerCase()))
					result.add(feature);
			}
		}
		return result;
	}
}