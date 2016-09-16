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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Help class which reads a list of characters
 * from a CSV file and creates a new features.xml
 * instance with the new ordering of characters.
 *
 */
public class OrderFeatureMatrix {

	public OrderFeatureMatrix() {
		super();
	}
	
	public void orderFeaturesFile(File orderFile, File featuresFile) 
		throws IOException {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		
		final Map<Character, FeatureSet> featureSets = fm.getFeatureSets();
		
		final CSVReader reader =
				new CSVReader(new InputStreamReader(new FileInputStream(orderFile), "UTF-8"));
		
		final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<feature_matrix xmlns=\"https://phon.ca/ns/features\"\n" + 
				"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"   xsi:schemaLocation=\"https://phon.ca/ns/features ../../../../xml/xsd/features.xsd\">";
		final StringBuffer xmlBuffer = new StringBuffer();
		xmlBuffer.append(prefix).append("\n");
		
		// feature information
		for(Feature feature:fm.getFeatureData()) {
			xmlBuffer.append("\t").append("<feature name=\"")
				     .append(feature.getName()).append("\"");
			
			if(feature.getPrimaryFamily() != FeatureFamily.UNDEFINED
					|| feature.getSecondaryFamily() != FeatureFamily.UNDEFINED
					|| feature.getSynonyms().length > 0) {
				xmlBuffer.append(">").append("\n");
				
				for(String syn:feature.getSynonyms()) {
					xmlBuffer.append("\t\t").append("<synonym>")
							 .append(syn).append("</synonym>").append("\n");
				}
				
				if(feature.getPrimaryFamily() != FeatureFamily.UNDEFINED) {
					xmlBuffer.append("\t\t").append("<primary_family>")
							 .append(feature.getPrimaryFamily().value())
							 .append("</primary_family>").append("\n");
				}
				
				if(feature.getSecondaryFamily() != FeatureFamily.UNDEFINED) {
					xmlBuffer.append("\t\t").append("<secondary_family>")
							 .append(feature.getSecondaryFamily().value())
							 .append("</secondary_family>").append("\n");
				}
				
				xmlBuffer.append("\t").append("</feature>");
			} else {
				xmlBuffer.append("/>");
			}
			xmlBuffer.append("\n");
		}
		xmlBuffer.append("\n");
		
		// feature sets as ordered by input file
		String[] line = null;
		while((line = reader.readNext()) != null) {
			String code = line[1];
			Integer val = Integer.parseInt(code.substring(2), 16);
			Character c = (char)val.intValue();
			
			
			FeatureSet featureSet = fm.getFeatureSet(c);
			featureSets.remove(c);
			
			// write comment
			xmlBuffer.append("\t").append("<!-- ");
			
			if(featureSet.hasFeature("diacritic")) {
				xmlBuffer.append("\u25cc");
			}
			xmlBuffer.append(c);
			xmlBuffer.append(" (").append(code).append(")").append(" -->").append("\n");
			
			xmlBuffer.append("\t").append("<feature_set char=\"&x")
				     .append(Integer.toHexString(val)).append(";\">");
			for(String feature:featureSet.getFeatures()) {
				xmlBuffer.append(feature).append(" ");
			}
			xmlBuffer.append("</feature_set>").append("\n");
		}
		reader.close();
		xmlBuffer.append("</feature_matrix>").append("\n");
		
		final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(featuresFile), "UTF-8"));
		out.write(xmlBuffer.toString());
		out.flush();
		out.close();
		
		featureSets.keySet().forEach( (key) -> System.out.println("Missing: " + key + Integer.toHexString(key.charValue())));
	}
	
}
