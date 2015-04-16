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
package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import ca.phon.ipa.features.FeatureSet;

public class FeatureGroup {	
	private String name;
	private String family;
	private FeatureSet features;
	
	/**
	 * Hidden constructor. All construction done in the static block below.
	 * @param name      the name of the feature group
	 * @param features  the list of features in the group
	 */
	private FeatureGroup(String name, String family, String[] features) {
		this.name = name;
		this.family = family;
		this.features = new FeatureSet();
		for(String feature : features)
			this.features.addFeature(feature);
	}
	
	/**
	 * Get the name of this feature group.
	 * @return  the name of the group
	 */
	public String getName() { return this.name; }
	
	/**
	 * Get the name of the family of this feature group.
	 * @return  the name of the group's family
	 */
	public String getFamily() { return this.family; }
	
	/**
	 * Determine if a specific feature is contained in this group.
	 * @param feature  the feature to check for
	 * @return         true if <code>feature</code> is contained in this
	 *                 feature group, false otherwise
	 */
	public boolean hasFeature(String feature) {
		return this.features.hasFeature(feature);
	}
	
	/**
	 * Get the feature set for this group.
	 * @return  a {@link FeatureSet} 
	 */
	public FeatureSet getFeatureSet() {
		return this.features;
	}
	
	/**
	 * Get the complete set of features for this group.
	 * @return  a {@link Collection<String>} of features 
	 */
	public Collection<String> getFeatures() {
		return this.features.getFeatures();
	}
	
	/*
	 * Construct the groupings.
	 */
	private static ArrayList<FeatureGroup> featureGroups;
	static {
		featureGroups = new ArrayList<FeatureGroup>();
		featureGroups.add(new FeatureGroup(
				"Voicing",
				"Manner",
				new String[] {
					"Voiced", "Voiceless",
					"Aspirated", "Weakly/Aspirated",
					"Unaspirated", "Unreleased"
				}
		));
		
		featureGroups.add(new FeatureGroup(
				"Labial",
				"Place",
				new String[] {
					"Labial", "Labiodental",
					"Bilabial", "LinguoLabial",
					"Round"
				}
		));
		
		featureGroups.add(new FeatureGroup(
				"Dorsal",
				"Place",
				new String[] {
					"Palatal", "Dorsal",
					"Velar", "Uvular",
					"Pharyngeal", "Laryngeal",
					"Glottal", "Epiglottal"
				}
		));
		
		featureGroups.add(new FeatureGroup(
				"Coronal",
				"Place",
				new String[] {
					"Dental", "Apical",
					"Laminal", "Coronal",
					"Anterior", "Interdental",
					"Alveolar", "Postalveolar",
					"Retroflex", "Distributed"
				}
		));
		
		featureGroups.add(new FeatureGroup(
				"Continuancy",
				"Manner",
				new String[] {
					"Approximant", "Liquid",
					"Lateral", "Rhotic",
					"Stop", "Continuant",
					"Obstruent", "Sonorant",
					"Fricative", "Affricate",
					"Click", "Implosive",
					"Flap", "Tap",
					"Trill", "Ejective"
				}
		));
		
		featureGroups.add(new FeatureGroup(
				"Nasality",
				"Manner",
				new String[] {
					"Nasal", "Prenasalized"
				}
		));
		
		featureGroups.add(new FeatureGroup(
				"Stridency",
				"Manner",
				new String[] {
					"Strident", "Sibilant"
				}
		));
	}

	/**
	 * Get the feature group containing a specified feature.
	 * @param feat  the feature to look for
	 * @return      a {@link java.util.Collection<FeatureGroup>} if any
	 * 				group contained the specified feature, null otherwise
	 */
	public static FeatureGroup getFeatureGroup(String feat) {
		for(FeatureGroup fg : featureGroups)
			if(fg.hasFeature(feat))
				return fg;
		return null;
	}
	
	/**
	 * Get a mapping of families to their respective children features.
	 * @return      a {@link java.util.Map<FeatureSet>} if any
	 * 				group contained the specified feature, null otherwise
	 */
	public static Map<String, FeatureSet> getFeatureMapping() {
		Hashtable<String, FeatureSet> mapping =
			new Hashtable<String, FeatureSet>();
		for(FeatureGroup fg : featureGroups) {
			// Add mapping to group
			mapping.put(fg.getName(), fg.getFeatureSet());
			
			// Extend feature set for family
			FeatureSet fsFamily = new FeatureSet();
			if(mapping.containsKey(fg.getFamily()))
				fsFamily = mapping.get(fg.getFamily());
			
			fsFamily = FeatureSet.union(fsFamily, fg.getFeatureSet());
			mapping.put(fg.getFamily(), fsFamily);
		}
		return mapping;
	}
	
	/**
	 * Get the feature group containing a specified feature.
	 * @param feat  the feature to look for
	 * @return      a {@link java.util.Collection<FeatureGroup>} if any
	 * 				group contained the specified feature, null otherwise
	 */
	public static FeatureSet getFeaturesInSameFamily(String feat) {
		String family = null;
		for(FeatureGroup fg : featureGroups) {
			if(fg.hasFeature(feat)) {
				family = fg.getFamily();
				break;
			}
		}
		
		FeatureSet result = new FeatureSet();
		if(family != null) {
			for(FeatureGroup fg : featureGroups)
				if(fg.getFamily().equals(family))
					result = FeatureSet.union(result, fg.getFeatureSet());
		}
		return result;
	}
	
	/**
	 * Get a collection of all of the feature groups.
	 * @return  a {@link java.util.Collection<FeatureGroup>}
	 */
	public static Collection<FeatureGroup> getFeatureGroups() {
		return featureGroups;
	}
}
