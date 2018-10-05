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
