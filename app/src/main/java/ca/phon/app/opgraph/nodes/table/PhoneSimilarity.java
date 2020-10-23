/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes.table;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.ipa.features.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.query.report.datasource.*;

/**
 * Calculate feature similarity between two {@link IPATranscript}s.
 *
 */
@OpNodeInfo(
		name="Phone Similarity",
		category="IPA Table Analysis",
		description="Calculate similarity between phones/transcripts based on features.  Requires columns 'IPA Target', 'IPA Actual' and 'Alignment' from the Phones query.",
		showInLibrary=true
)
public class PhoneSimilarity extends TableOpNode implements NodeSettings {

	private JPanel settingsPanel;

	private JCheckBox placeBox;
	private boolean includePlace = true;
	private InputField placeInputField =
			new InputField("includePlace", "Include place features", true, true, Boolean.class);

	private JCheckBox mannerBox;
	private boolean includeManner = true;
	private InputField mannerInputField =
			new InputField("includeManner", "Include manner features", true, true, Boolean.class);

	private JCheckBox voicingBox;
	private boolean includeVoicing = true;
	private InputField voicingInputField =
			new InputField("includeVoicing", "Include voicing features", true, true, Boolean.class);

	private JCheckBox heightBox;
	private boolean includeHeight = true;
	private InputField heightInputField =
			new InputField("includeHeight", "Include height features", true, true, Boolean.class);

	private JCheckBox backnessBox;
	private boolean includeBackness = true;
	private InputField backnessInputField =
			new InputField("includeBackness", "Include backness features", true, true, Boolean.class);

	private JCheckBox tensenessBox;
	private boolean includeTenseness = true;
	private InputField tensenessInputField =
			new InputField("includeTenseness", "Include tenseness features", true, true, Boolean.class);

	private JCheckBox roundingBox;
	private boolean includeRounding = true;
	private InputField roundingInputField =
			new InputField("includeRounding", "Include rounding features", true, true, Boolean.class);

	private class FeatureSimilarity {
		int numFeatures = 0;
		int numMatched = 0;
	}

	public PhoneSimilarity() {
		super();

		putField(placeInputField);
		putField(mannerInputField);
		putField(voicingInputField);

		putField(heightInputField);
		putField(backnessInputField);
		putField(tensenessInputField);
		putField(roundingInputField);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(super.tableInput);
		if(table == null) throw new ProcessingException(null, new NullPointerException("table"));

		// check for required columns
		final int targetColIdx = table.getColumnIndex("IPA Target");
		final int actualColIdx = table.getColumnIndex("IPA Actual");
		final int alignColIdx = table.getColumnIndex("Alignment");

		if(targetColIdx < 0)
			throw new ProcessingException(null, "Missing required column: IPA Target");
		if(actualColIdx < 0)
			throw new ProcessingException(null, "Missing required column: IPA Actual");
		if(alignColIdx < 0)
			throw new ProcessingException(null, "Missing required column: Alignment");

		// setup output table
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		int colIdx = 0;
		for(; colIdx < table.getColumnCount(); colIdx++) {
			outputTable.setColumnTitle(colIdx, table.getColumnTitle(colIdx));
		}
		final int featuresColIdx = colIdx;
		final Set<PhoneDimension> dimensions = new LinkedHashSet<>();
		
		boolean includePlace =
				(context.get(placeInputField) != null ? (Boolean)context.get(placeInputField) : isIncludePlace());
		if(includePlace) {
			dimensions.add(PhoneDimension.PLACE);
		}
		boolean includeManner =
				(context.get(mannerInputField) != null ? (Boolean)context.get(mannerInputField) : isIncludeManner());
		if(includeManner) {
			dimensions.add(PhoneDimension.MANNER);
		}
		boolean includeVoicing =
				(context.get(voicingInputField) != null? (Boolean)context.get(voicingInputField) : isIncludeVoicing());
		if(includeVoicing) {
			dimensions.add(PhoneDimension.VOICING);
		}

		boolean includeHeight =
				(context.get(heightInputField) != null ? (Boolean)context.get(heightInputField) : isIncludeHeight());
		if(includeHeight) {
			dimensions.add(PhoneDimension.HEIGHT);
		}
		boolean includeBackness =
				(context.get(backnessInputField) != null ? (Boolean)context.get(backnessInputField) : isIncludeBackness());
		if(includeBackness) {
			dimensions.add(PhoneDimension.BACKNESS);
		}
		boolean includeTenseness =
				(context.get(tensenessInputField) != null ? (Boolean)context.get(tensenessInputField) : isIncludeTenseness());
		if(includeTenseness) {
			dimensions.add(PhoneDimension.TENSENESS);
		}
		boolean includeRounding =
				(context.get(roundingInputField) != null ? (Boolean)context.get(roundingInputField) : isIncludeRounding());
		if(includeRounding) {
			dimensions.add(PhoneDimension.ROUNDING);
		}
		
		dimensions.forEach((dim) -> {
			outputTable.setColumnTitle(outputTable.getColumnCount(), "#Fs " + dim);
			outputTable.setColumnTitle(outputTable.getColumnCount(), "Sim " + dim);
		});
		
		outputTable.setColumnTitle(outputTable.getColumnCount(), "#Fs");
		outputTable.setColumnTitle(outputTable.getColumnCount(), "Sim");
		

		for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
			Object[] rowData = table.getRow(rowIdx);
			Object[] newRow = Arrays.copyOf(rowData, outputTable.getColumnCount());

			final IPATranscript ipaTarget = (IPATranscript)rowData[targetColIdx];
			final IPATranscript ipaActual = (IPATranscript)rowData[actualColIdx];
			final String alignment = (String)rowData[alignColIdx];

			final Map<PhoneDimension, FeatureSimilarity> sim = calculateSimilarity(ipaTarget, ipaActual, alignment,
					includePlace, includeManner, includeVoicing,
					includeHeight, includeBackness, includeTenseness, includeRounding);
			
			int totalFs = 0;
			int totalMatched = 0;
			colIdx = featuresColIdx;
			for(PhoneDimension dim:dimensions) {
				FeatureSimilarity s = sim.get(dim);
				totalFs += s.numFeatures;
				totalMatched += s.numMatched;
				
				newRow[colIdx++] = new Integer(s.numFeatures);
				newRow[colIdx++] = (s.numFeatures > 0 ? new Float( ((float)s.numMatched / (float)s.numFeatures) * 100.0f ) : new Float(0.0f));
			}
			newRow[colIdx++] = new Integer(totalFs);
			newRow[colIdx++] = (totalFs > 0 ? new Float( ((float)totalMatched/(float)totalFs) * 100.0f) : new Float(0.0f) );
						
			outputTable.addRow(newRow);
		}

		context.put(super.tableOutput, outputTable);
	}

	/**
	 * Returns the full set of features used when comparing phones as
	 * setup by the node's options.
	 *
	 * @return
	 */
	public FeatureSet getComparsionFeatures(boolean includePlace, boolean includeManner, boolean includeVoicing,
			boolean includeHeight, boolean includeBackness, boolean includeTenseness, boolean includeRounding) {
		FeatureSet retVal = new FeatureSet();

		if(includePlace)
			retVal = FeatureSet.union(retVal, PhoneDimension.PLACE.getFeatures());
		if(includeManner)
			retVal = FeatureSet.union(retVal, PhoneDimension.MANNER.getFeatures());
		if(includeVoicing)
			retVal = FeatureSet.union(retVal, PhoneDimension.VOICING.getFeatures());

		if(includeHeight)
			retVal = FeatureSet.union(retVal, PhoneDimension.HEIGHT.getFeatures());
		if(includeBackness)
			retVal = FeatureSet.union(retVal, PhoneDimension.BACKNESS.getFeatures());
		if(includeTenseness)
			retVal = FeatureSet.union(retVal, PhoneDimension.TENSENESS.getFeatures());
		if(includeRounding)
			retVal = FeatureSet.union(retVal, PhoneDimension.ROUNDING.getFeatures());

		return retVal;
	}

	private Map<PhoneDimension, FeatureSimilarity> calculateSimilarity(IPATranscript a, IPATranscript b, String alignment,
			boolean includePlace, boolean includeManner, boolean includeVoicing,
			boolean includeHeight, boolean includeBackness, boolean includeTenseness, boolean includeRounding) {
		final PhoneMap phoneMap = PhoneMap.fromString(a, b, alignment);

		final Map<PhoneDimension, FeatureSimilarity> retVal = new HashMap<>();
		for(PhoneDimension dim:PhoneDimension.values())
			retVal.put(dim, new FeatureSimilarity());
		
		for(int i = 0; i < phoneMap.getAlignmentLength(); i++) {
			List<IPAElement> alignedEles = phoneMap.getAlignedElements(i);
			Map<PhoneDimension, FeatureSimilarity> phoneSim = calculateSimilarity(alignedEles.get(0), alignedEles.get(1),
					includePlace, includeManner, includeVoicing,
					includeHeight, includeBackness, includeTenseness, includeRounding);
			for(PhoneDimension dim:phoneSim.keySet()) {
				FeatureSimilarity fs = retVal.get(dim);
				FeatureSimilarity sim = phoneSim.get(dim);
				
				fs.numFeatures += sim.numFeatures;
				fs.numMatched += sim.numMatched;
			}
		}

		return retVal;
	}

	private Map<PhoneDimension, FeatureSimilarity> calculateSimilarity(IPAElement a, IPAElement b,
			boolean includePlace, boolean includeManner, boolean includeVoicing,
			boolean includeHeight, boolean includeBackness, boolean includeTenseness, boolean includeRounding) {
		final Map<PhoneDimension, FeatureSimilarity> retVal = new HashMap<>();
		
		if(includePlace)
			retVal.put(PhoneDimension.PLACE, calculateSimilarity(a, b, PhoneDimension.PLACE));
		if(includeManner)
			retVal.put(PhoneDimension.MANNER, calculateSimilarity(a, b, PhoneDimension.MANNER));
		if(includeVoicing)
			retVal.put(PhoneDimension.VOICING, calculateSimilarity(a, b, PhoneDimension.VOICING));
		
		if(includeHeight)
			retVal.put(PhoneDimension.HEIGHT, calculateSimilarity(a, b, PhoneDimension.HEIGHT));
		if(includeBackness)
			retVal.put(PhoneDimension.BACKNESS, calculateSimilarity(a, b, PhoneDimension.BACKNESS));
		if(includeTenseness)
			retVal.put(PhoneDimension.TENSENESS, calculateSimilarity(a, b, PhoneDimension.TENSENESS));
		if(includeRounding)
			retVal.put(PhoneDimension.ROUNDING, calculateSimilarity(a, b, PhoneDimension.ROUNDING));
		
		return retVal;
	}
	
	private FeatureSimilarity calculateSimilarity(IPAElement a, IPAElement b, PhoneDimension dim) {
		FeatureSimilarity retVal = new FeatureSimilarity();
		
		if(a == null || b == null) {
			FeatureSet features = (a != null ? a.getFeatureSet() : b.getFeatureSet() );
			features = FeatureSet.intersect(features, dim.getFeatures());
			
			retVal.numFeatures = features.size();
		} else {
			final FeatureSet aFeatures = FeatureSet.intersect(a.getFeatureSet(), dim.getFeatures());
			final FeatureSet bFeatures = FeatureSet.intersect(b.getFeatureSet(), dim.getFeatures());
			
			final FeatureSet sameFeatures = FeatureSet.intersect(aFeatures, bFeatures);
			
			retVal.numFeatures = Math.max(aFeatures.size(), bFeatures.size());
			retVal.numMatched = sameFeatures.size();
		}
		
		return retVal;
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());

			placeBox = new JCheckBox("Place");
			placeBox.setSelected(includePlace);
			settingsPanel.add(placeBox);

			mannerBox = new JCheckBox("Manner");
			mannerBox.setSelected(includeManner);
			settingsPanel.add(mannerBox);

			voicingBox = new JCheckBox("Vocing");
			voicingBox.setSelected(includeVoicing);
			settingsPanel.add(voicingBox);

			heightBox = new JCheckBox("Height");
			heightBox.setSelected(includeHeight);
			settingsPanel.add(heightBox);

			backnessBox = new JCheckBox("Backness");
			backnessBox.setSelected(includeBackness);
			settingsPanel.add(backnessBox);

			tensenessBox = new JCheckBox("Tenseness");
			tensenessBox.setSelected(includeTenseness);
			settingsPanel.add(tensenessBox);
		}
		return settingsPanel;
	}

	public boolean isIncludePlace() {
		return (placeBox != null ? placeBox.isSelected() : includePlace);
	}

	public void setIncludePlace(boolean includePlace) {
		this.includePlace = includePlace;
		if(this.placeBox != null)
			this.placeBox.setSelected(includePlace);
	}

	public boolean isIncludeManner() {
		return (mannerBox != null ? mannerBox.isSelected() : includeManner);
	}

	public void setIncludeManner(boolean includeManner) {
		this.includeManner = includeManner;
		if(this.mannerBox != null)
			this.mannerBox.setSelected(includeManner);
	}

	public boolean isIncludeVoicing() {
		return (voicingBox != null ? voicingBox.isSelected() : includeVoicing);
	}

	public void setIncludeVoicing(boolean includeVoicing) {
		this.includeVoicing = includeVoicing;
		if(this.voicingBox != null)
			this.voicingBox.setSelected(includeVoicing);
	}

	public boolean isIncludeHeight() {
		return (this.heightBox != null ? heightBox.isSelected() : this.includeHeight);
	}

	public void setIncludeHeight(boolean includeHeight) {
		this.includeHeight = includeHeight;
		if(this.heightBox != null)
			this.heightBox.setSelected(includeHeight);
	}

	public boolean isIncludeBackness() {
		return (this.backnessBox != null ? this.backnessBox.isSelected() : this.includeBackness);
	}

	public void setIncludeBackness(boolean includeBackness) {
		this.includeBackness = includeBackness;
		if(this.backnessBox != null)
			this.backnessBox.setSelected(includeBackness);
	}

	public boolean isIncludeTenseness() {
		return (this.tensenessBox != null ? this.tensenessBox.isSelected() : this.includeTenseness);
	}

	public void setIncludeTenseness(boolean includeTenseness) {
		this.includeTenseness = includeTenseness;
		if(this.tensenessBox != null)
			this.tensenessBox.setSelected(includeTenseness);
	}

	public boolean isIncludeRounding() {
		return (this.roundingBox != null ? this.roundingBox.isSelected() : this.includeRounding);
	}

	public void setIncludeRounding(boolean includeRounding) {
		this.includeRounding = includeRounding;
		if(this.roundingBox != null)
			this.roundingBox.setSelected(includeRounding);
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();

		props.put(PhoneSimilarity.class.getName() + ".includePlace", isIncludePlace());
		props.put(PhoneSimilarity.class.getName() + ".includeManner", isIncludeManner());
		props.put(PhoneSimilarity.class.getName() + ".includeVoicing", isIncludeVoicing());

		props.put(PhoneSimilarity.class.getName() + ".includeHeight", isIncludeHeight());
		props.put(PhoneSimilarity.class.getName() + ".includeBackness", isIncludeBackness());
		props.put(PhoneSimilarity.class.getName() + ".includeTenseness", isIncludeTenseness());
		props.put(PhoneSimilarity.class.getName() + ".includeRounding", isIncludeRounding());

		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIncludePlace(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includePlace", "true")));
		setIncludeManner(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeManner", "true")));
		setIncludeVoicing(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeVoicing", "true")));

		setIncludeHeight(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeHeight", "true")));
		setIncludeBackness(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeBackness", "true")));
		setIncludeTenseness(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeTenseness", "true")));
		setIncludeRounding(Boolean.parseBoolean(properties.getProperty(PhoneSimilarity.class.getName() + ".includeRounding", "true")));
	}
}
