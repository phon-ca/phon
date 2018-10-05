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

import java.util.List;

/**
 * A phonetic feature
 *
 */
public class Feature {

	/**
	 * Feature name
	 */
	private String name;
	
	/**
	 * Feature synonyms
	 */
	private String[] synonyms = new String[0];
	
	/**
	 * Primary family
	 */
	private FeatureFamily primaryFamily = FeatureFamily.UNDEFINED;
	
	/**
	 * Secondary family
	 */
	private FeatureFamily secondaryFamily = FeatureFamily.UNDEFINED;
	
	/**
	 * Feature set (mask)
	 */
	private FeatureSet fs;
	
	/**
	 * Constructors
	 */
	public Feature(String name) {
		this.name = name;
	}
	
	public Feature(String name, List<String> synonyms) {
		this(name, synonyms.toArray(new String[0]));
	}
	
	public Feature(String name, String[] synonyms) {
		this.name = name;
		this.synonyms = synonyms;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}

	public FeatureFamily getPrimaryFamily() {
		return primaryFamily;
	}

	public void setPrimaryFamily(FeatureFamily primaryFamily) {
		this.primaryFamily = primaryFamily;
	}

	public FeatureFamily getSecondaryFamily() {
		return secondaryFamily;
	}

	public void setSecondaryFamily(FeatureFamily secondaryFamily) {
		this.secondaryFamily = secondaryFamily;
	}
	
	public FeatureSet getFeatureSet() {
		return this.fs;
	}
	
	public void setFeatureSet(FeatureSet fs) {
		this.fs = fs;
	}
	
}
