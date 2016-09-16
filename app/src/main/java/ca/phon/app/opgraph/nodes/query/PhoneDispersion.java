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
package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jdesktop.swingx.JXTitledSeparator;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.features.IPAElementComparator;
import ca.phon.query.report.datasource.DefaultTableDataSource;

@OpNodeInfo(
		name="Phone Dispersion",
		description="Calculate global or feature dispersion for a given aligned phone inventory",
		category="Report",
		showInLibrary=true
)
public class PhoneDispersion extends TableOpNode implements NodeSettings {
	
	private JPanel settingsPanel;
	
	private ButtonGroup typeButtonGroup;
	private JRadioButton globalVariabilityButton;
	private JRadioButton featureVariabilityButton;

	private JCheckBox placeBox;
	private JRadioButton majorPlaceBox;
	private JRadioButton allPlaceBox;
	private JCheckBox mannerBox;
	private JCheckBox voicingBox;
	
	private boolean useFeatureVariability = false;
	
	private boolean includePlace = true;
	
	private boolean useMajorPlace = true;
	
	private boolean includeManner = true;
	
	private boolean includeVoicing = true;
	
	private final FeatureSet MAJOR_PLACE =
			FeatureSet.fromArray(new String[]{"Labial", "Coronal", "Dorsal", "Guttural"});
	
	public PhoneDispersion() {
		super();
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		// input table should be in the format of an inventory with
		// IPA Target/IPA Actual columns included.
		final DefaultTableDataSource inputTable =
				(DefaultTableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		
		// check for required columns
		int ipaTargetColIdx = inputTable.getColumnIndex("IPA Target");
		if(ipaTargetColIdx < 0) {
			throw new ProcessingException(null, "Phone Dispersion requires an IPA Target column");
		}
		
		int ipaActualColIdx = inputTable.getColumnIndex("IPA Actual");
		if(ipaActualColIdx < 0) {
			throw new ProcessingException(null, "Phone Dispersion requires an IPA Acutal column");
		}
		
		// all columns after IPA Actual should have Integer types
		for(int col = ipaActualColIdx+1; col < inputTable.getColumnCount(); col++) {
			if(inputTable.inferColumnType(col).isAssignableFrom(Number.class)) {
				throw new ProcessingException(null, "Phone Dispersion requires an inventory table as input");
			}
		}
		
		int numInventoryCols = inputTable.getColumnCount() - (ipaActualColIdx+1);
		
		// IPA Target totals
		final Map<IPAElement, long[]> totals = new TreeMap<>(new IPAElementComparator());
		final Map<IPAElement, long[]> subCosts = new TreeMap<>(new IPAElementComparator());
		
		for(int row = 0; row < inputTable.getRowCount(); row++) {
			checkCanceled();
			
			IPATranscript ipaTargetVal = (IPATranscript)inputTable.getValueAt(row, ipaTargetColIdx);
			IPATranscript ipaActualVal = (IPATranscript)inputTable.getValueAt(row, ipaActualColIdx);
			
			IPAElement ipaTEle = (ipaTargetVal.length() > 0 ? ipaTargetVal.elementAt(0) : null);
			IPAElement ipaAEle = (ipaActualVal.length() > 0 ? ipaActualVal.elementAt(0) : null);
			
			long[] sums = totals.get(ipaTEle);
			if(sums == null) {
				sums =  new long[numInventoryCols];
				totals.put(ipaTEle, sums);
			}
			for(int col = 0; col < numInventoryCols; col++) {
				sums[col] += ((Number)inputTable.getValueAt(row, ipaActualColIdx + (col+1))).longValue();
			}
			
			final int cost = calculateWeight(ipaTEle, ipaAEle);
			long[] costs = subCosts.get(ipaTEle);
			if(costs == null) {
				costs = new long[numInventoryCols];
				subCosts.put(ipaTEle, costs);
			}
			for(int col = 0; col < numInventoryCols; col++) {
				long colVal = ((Number)inputTable.getValueAt(row, ipaActualColIdx + (col+1))).longValue();
				costs[col] += (cost * colVal);
			}
		}
		
		int dimensions = numDimensions();
		for(IPAElement ele:totals.keySet()) {
			checkCanceled();
			
			Object rowData[] = new Object[1 + numInventoryCols];
			rowData[0] = new IPATranscript(ele);
			
			long sums[] = totals.get(ele);
			long costs[] = subCosts.get(ele);
			
			for(int col = 0; col < numInventoryCols; col++) {
				rowData[col+1] = (sums[col] > 0 
						?	(float)(costs[col])/(float)(sums[col]*dimensions)
						:	0);
			}
			outputTable.addRow(rowData);
		}
		
		// setup column names
		outputTable.setColumnTitle(0, "IPA Target");
		for(int col = 0; col < numInventoryCols; col++) {
			outputTable.setColumnTitle(col+1, inputTable.getColumnTitle(ipaActualColIdx+(col+1)));
		}
		
		context.put(tableOutput, outputTable);
	}
	
	protected int numDimensions() {
		int retVal = 0;
		if(isUseFeatureVariability()) {
			if(isIncludePlace()) ++retVal;
			if(isIncludeManner()) ++retVal;
			if(isIncludeVoicing()) ++retVal;
		} else {
			retVal = 1;
		}
		return retVal;
	}
	
	protected int deletionCost() {
//		if(isUseFeatureVariability()) {
//			int cost = 1;
//			if(isIncludePlace()) ++cost;
//			if(isIncludeManner()) ++cost;
//			if(isIncludeVoicing()) ++cost;
//			return cost;
//		} else {
//			return 2;
//		}
		return 0;
	}
	
	/**
	 * <p>Calculate the substitution cost for the given model actual pair.</p>
	 * 
	 * <p>For global variability, cost values are:
	 * <ul>
	 * <li><b>Deletion</b> = 2</li>
	 * <li><b>Substitution</b> = 1</li>
	 * </ul>
	 * </p>
	 * 
	 * @param model
	 * @param actual
	 * @return
	 */
	protected int calculateWeight(IPAElement model, IPAElement actual) {
		int retVal = 0;
		
		if(actual == null) return deletionCost();
		
		if(isUseFeatureVariability()) {
			if(isIncludePlace()) {
				FeatureSet modelPlace = 
						(isUseMajorPlace() 
							? FeatureSet.intersect(model.getFeatureSet(), MAJOR_PLACE)
							: model.getFeatureSet().getPlace());
				FeatureSet actualPlace = 
						(isUseMajorPlace()
							? FeatureSet.intersect(actual.getFeatureSet(), MAJOR_PLACE)
							: actual.getFeatureSet().getPlace());
				
				FeatureSet intersection = FeatureSet.intersect(modelPlace, actualPlace);
				if(!intersection.equals(modelPlace)) {
					++retVal;
				}
			}
			if(isIncludeManner()) {
				FeatureSet modelManner = model.getFeatureSet().getManner();
				FeatureSet actualManner = actual.getFeatureSet().getManner();
				
				FeatureSet intersection = FeatureSet.intersect(modelManner, actualManner);
				if(!intersection.equals(modelManner)) {
					++retVal;
				}
			}
			if(isIncludeVoicing()) {
				FeatureSet modelVoicing = model.getFeatureSet().getVoicing();
				FeatureSet actualVoicing = actual.getFeatureSet().getVoicing();
				
				FeatureSet intersection = FeatureSet.intersect(modelVoicing, actualVoicing);
				if(!intersection.equals(modelVoicing)) {
					++retVal;
				}
			}
		} else {
			if(!model.toString().equals(actual.toString())) {
				++retVal;
			}
		}
		
		return retVal;
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}
	
	private JPanel createSettingsPanel() {
		final JPanel retVal = new JPanel();
		final GridBagLayout layout = new GridBagLayout();
		retVal.setLayout(layout);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		
		retVal.add(new JXTitledSeparator("Phone Dispersion Options"), gbc);
		
		typeButtonGroup = new ButtonGroup();
		globalVariabilityButton = new JRadioButton("Global Dispersion");
		featureVariabilityButton = new JRadioButton("Feature Dispersion");
		globalVariabilityButton.setSelected(!useFeatureVariability);
		featureVariabilityButton.setSelected(useFeatureVariability);
		typeButtonGroup.add(globalVariabilityButton);
		typeButtonGroup.add(featureVariabilityButton);
		
		++gbc.gridy;
		retVal.add(globalVariabilityButton, gbc);
		++gbc.gridy;
		retVal.add(featureVariabilityButton, gbc);
		
		placeBox = new JCheckBox("Place");
		placeBox.setSelected(includePlace);
		placeBox.setEnabled(useFeatureVariability);
		
		mannerBox = new JCheckBox("Manner");
		mannerBox.setSelected(includeManner);
		mannerBox.setEnabled(useFeatureVariability);
		
		voicingBox = new JCheckBox("Voicing");
		voicingBox.setSelected(includeVoicing);
		voicingBox.setEnabled(useFeatureVariability);
		
		gbc.insets = new Insets(2, 20, 2, 2);
		++gbc.gridy;
		retVal.add(placeBox, gbc);
		++gbc.gridy;
		retVal.add(mannerBox, gbc);
		++gbc.gridy;
		retVal.add(voicingBox, gbc);
		
		++gbc.gridy;
		gbc.weighty = 1.0;
		retVal.add(Box.createVerticalGlue(), gbc);
		
		final ActionListener btnListener = (e) -> {
			setUseFeatureVariability(featureVariabilityButton.isSelected());
		};
		featureVariabilityButton.addActionListener(btnListener);
		globalVariabilityButton.addActionListener(btnListener);
		
		return retVal;
	}
	
	public boolean isUseFeatureVariability() {
		return (this.featureVariabilityButton != null ? this.featureVariabilityButton.isSelected() : this.useFeatureVariability);
	}
	
	public void setUseFeatureVariability(boolean useFeatureVariability) {
		this.useFeatureVariability = useFeatureVariability;
		if(this.featureVariabilityButton != null) {
			this.featureVariabilityButton.setSelected(useFeatureVariability);
			placeBox.setEnabled(useFeatureVariability);
			mannerBox.setEnabled(useFeatureVariability);
			voicingBox.setEnabled(useFeatureVariability);
		}
	}
	
	public boolean isIncludePlace() {
		return (this.placeBox != null ? this.placeBox.isSelected() : this.includePlace);
	}
	
	public void setIncludePlace(boolean includePlace) {
		this.includePlace = includePlace;
		if(this.placeBox != null)
			this.placeBox.setSelected(includePlace);
	}
	
	public boolean isUseMajorPlace() {
		return (this.majorPlaceBox != null ? this.majorPlaceBox.isSelected() : this.useMajorPlace);
	}
	
	public void setUseMajorPlace(boolean useMajorPlace) {
		this.useMajorPlace = useMajorPlace;
		if(this.majorPlaceBox != null)
			this.majorPlaceBox.setSelected(useMajorPlace);
	}
	
	public boolean isIncludeManner() {
		return (this.mannerBox != null ? this.mannerBox.isSelected() : this.includeManner);
	}
	
	public void setIncludeManner(boolean includeManner) {
		this.includeManner = includeManner;
		if(this.mannerBox != null)
			this.mannerBox.setSelected(includeManner);
	}
	
	public boolean isIncludeVoicing() {
		return (this.voicingBox != null ? this.voicingBox.isSelected() : this.includeVoicing);
	}
	
	public void setIncludeVoicing(boolean includeVoicing) {
		this.includeVoicing = includeVoicing;
		if(this.voicingBox != null)
			this.voicingBox.setSelected(includeVoicing);
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		
		props.setProperty("useFeatureVariability", Boolean.toString(isUseFeatureVariability()));
		props.setProperty("includePlace", Boolean.toString(isIncludePlace()));
		props.setProperty("useMajorPlace", Boolean.toString(isUseMajorPlace()));
		props.setProperty("includeManner", Boolean.toString(isIncludeManner()));
		props.setProperty("includeVoicing", Boolean.toString(isIncludeVoicing()));
		
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		setUseFeatureVariability(
				Boolean.parseBoolean(properties.getProperty("useFeatureVariability", "false")));
		setIncludePlace(
				Boolean.parseBoolean(properties.getProperty("includePlace", "true")));
		setUseMajorPlace(
				Boolean.parseBoolean(properties.getProperty("useMajorPlace", "true")));
		setIncludeManner(
				Boolean.parseBoolean(properties.getProperty("includeManner", "true")));
		setIncludeVoicing(
				Boolean.parseBoolean(properties.getProperty("includeVoicing", "true")));
	}

}
