/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
