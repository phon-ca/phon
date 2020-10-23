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

import ca.phon.app.opgraph.wizard.*;
import ca.phon.ipa.*;
import ca.phon.ipa.features.*;
import ca.phon.ipa.tree.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.query.report.datasource.*;
import ca.phon.ui.text.*;

@OpNodeInfo(
		name="Feature Accuracy",
		category="IPA Table Analysis",
		description="Inventory of IPA Target values with num accurate, substituted, and deleted columns.",
		showInLibrary = true
)
public class FeatureAccuracyNode extends TableOpNode implements NodeSettings {

	private JPanel settingsPanel;

	private JCheckBox ignoreDiacriticsBox;

	private JRadioButton globalAccuracyBox;

	private JRadioButton placeAccuracyBox;

	private JRadioButton mannerAccuracyBox;

	private JRadioButton customAccuracyBox;

	private PromptedTextField featureListField;

	private boolean ignoreDiacritics = false;

	// accuracy type
	private boolean globalAccuracy = true;

	// list of features for feature accuracy
	private String featureList = "PLACE";

	protected final OutputField accurateOutputTableField =
			new OutputField("accurate", "Table of accurate productions", true, TableDataSource.class);

	protected final OutputField substitutedOutputTableField =
			new OutputField("substitutions", "Table of substitutions", true, TableDataSource.class);

	protected final OutputField deletedOutputTableField =
			new OutputField("deleted", "Table of deletions", true, TableDataSource.class);

	public FeatureAccuracyNode() {
		super();

		putField(accurateOutputTableField);
		putField(substitutedOutputTableField);
		putField(deletedOutputTableField);

		putExtension(NodeSettings.class, this);
	}

	private DefaultTableDataSource copyTableSchema(DefaultTableDataSource src) {
		final DefaultTableDataSource retVal = new DefaultTableDataSource();

		for(int i = 0; i < src.getColumnCount(); i++) {
			retVal.setColumnTitle(i, src.getColumnTitle(i));
		}

		return retVal;
	}


	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		context.put(tableInput, outputTable);

		final DefaultTableDataSource accurateTable = copyTableSchema(table);
		final DefaultTableDataSource substitutedTable = copyTableSchema(table);
		final DefaultTableDataSource deletedTable = copyTableSchema(table);

		if(isGlobalAccuracy()) {
			calculateGlobalAccuracy(context, table, outputTable, accurateTable, substitutedTable, deletedTable);
		} else {
			calculateFeatureAccuracy(context, table, outputTable, accurateTable, substitutedTable, deletedTable);
		}

		context.put(accurateOutputTableField, accurateTable);
		context.put(substitutedOutputTableField, substitutedTable);
		context.put(deletedOutputTableField, deletedTable);
	}

	private void calculateGlobalAccuracy(OpContext context, DefaultTableDataSource table, DefaultTableDataSource outputTable,
			DefaultTableDataSource accurateTable, DefaultTableDataSource substitutedTable, DefaultTableDataSource deletedTable) {
		IpaTernaryTree<AccuracyInfo> totals = new IpaTernaryTree<>();
		final Map<String, IpaTernaryTree<AccuracyInfo>>
			accuracyMap = new TreeMap<>();

		boolean ignoreDiacritics = isIgnoreDiacritics();
		if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)) {
			ignoreDiacritics = (Boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION);
		}

		int ipaTargetCol = table.getColumnIndex("IPA Target");
		int ipaActualCol = table.getColumnIndex("IPA Actual");

		if(ipaTargetCol < 0 || ipaActualCol < 0) return;

		for(int row = 0; row < table.getRowCount(); row++) {

			IPATranscript ipaT = (IPATranscript)table.getValueAt(row, ipaTargetCol);
			if(ignoreDiacritics) {
				ipaT = ipaT.removePunctuation().stripDiacritics();
			}
			IPATranscript ipaA = (IPATranscript)table.getValueAt(row, ipaActualCol);
			if(ignoreDiacritics) {
				ipaA = ipaA.removePunctuation().stripDiacritics();
			}

			AccuracyInfo totalInfo = totals.get(ipaT);
			if(totalInfo == null) {
				totalInfo = new AccuracyInfo();
				totals.put(ipaT, totalInfo);
			}
			++totalInfo.count;

			boolean deleted = (ipaA == null || ipaA.length() == 0);
			boolean accurate = !deleted && ipaT.equals(ipaA);
			boolean substituted = !deleted && !accurate;

			if(deleted) {
				++totalInfo.deleted;
				deletedTable.addRow(table.getRow(row));
			}
			if(accurate) {
				++totalInfo.accurate;
				accurateTable.addRow(table.getRow(row));
			}
			if(substituted) {
				++totalInfo.substitutions;
				substitutedTable.addRow(table.getRow(row));
			}
		}

		// setup columns
		List<String> colNames = new ArrayList<>();
		colNames.add("IPA Target");
		colNames.add("Count");
		colNames.add("# Correct");
		colNames.add("# Substituted");
		colNames.add("# Deleted");
		for(int i = 0; i < colNames.size(); i++) outputTable.setColumnTitle(i, colNames.get(i));

		for(IPATranscript target:totals.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int col = 0;
			rowData[col++] = target;

			AccuracyInfo totalInfo = totals.get(target);
			rowData[col++] = totalInfo.count;
			rowData[col++] = totalInfo.accurate;
			rowData[col++] = totalInfo.substitutions;
			rowData[col++] = totalInfo.deleted;

			outputTable.addRow(rowData);
		}
	}

	private void calculateFeatureAccuracy(OpContext context, DefaultTableDataSource table, DefaultTableDataSource outputTable,
			DefaultTableDataSource accurateTable, DefaultTableDataSource substitutedTable, DefaultTableDataSource deletedTable) {
		String featureList = getFeatureList();
		if(featureList.equalsIgnoreCase("PLACE")) {
			featureList =  "labial,dental,alveolar,alveo-palatal,palatal,velar,uvular,pharyngeal,laryngeal";
		} else if(featureList.equalsIgnoreCase("MANNER")) {
			featureList = "stop,fricative,affricate,nasal,lateral,rhotic,glide";
		}

		String[] features = featureList.split(",");
		final FeatureSet fs = FeatureSet.fromArray(features);

		final Map<String, AccuracyInfo> totals = new TreeMap<>();
		final Map<String, Map<String, AccuracyInfo>> accuracyMap = new TreeMap<>();

		boolean ignoreDiacritics = isIgnoreDiacritics();
		if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)) {
			ignoreDiacritics = (Boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION);
		}

		int ipaTargetCol = table.getColumnIndex("IPA Target");
		int ipaActualCol = table.getColumnIndex("IPA Actual");

		if(ipaTargetCol < 0 || ipaActualCol < 0) return;

		for(int row = 0; row < table.getRowCount(); row++) {
			IPATranscript ipaT = (IPATranscript)table.getValueAt(row, ipaTargetCol);
			if(ignoreDiacritics) {
				ipaT = ipaT.removePunctuation().stripDiacritics();
			}
			if(ipaT.length() == 0) continue;

			IPATranscript ipaA = (IPATranscript)table.getValueAt(row, ipaActualCol);
			if(ignoreDiacritics) {
				ipaA = ipaA.removePunctuation().stripDiacritics();
			}

			Optional<String> featureNameOpt =
					FeatureSet.intersect(ipaT.elementAt(0).getFeatureSet(), fs).getFeatures().stream().findFirst();
			if(!featureNameOpt.isPresent()) continue;
			final String featureName = featureNameOpt.get();

			AccuracyInfo totalInfo = totals.get(featureName);
			if(totalInfo == null) {
				totalInfo = new AccuracyInfo();
				totals.put(featureName, totalInfo);
			}
			++totalInfo.count;
			// XXX this seems wrong, need to confirm expected output of this function
			if(ipaA == null || ipaA.length() == 0) {
				++totalInfo.deleted;
			} else {
				if(ipaT.equals(ipaA)) {
					++totalInfo.accurate;
				} else {
					++totalInfo.substitutions;
				}
			}
		}

		List<String> colNames = new ArrayList<>();
		colNames.add("Feature");
		colNames.add("Count");
		colNames.add("Accurate");
		colNames.add("Substituted");
		colNames.add("Deleted");

		for(int i = 0; i < colNames.size(); i++) outputTable.setColumnTitle(i, colNames.get(i));

		for(String feature:totals.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int col = 0;
			rowData[col++] = feature;
			AccuracyInfo totalInfo = totals.get(feature);
			rowData[col++] = totalInfo.count;
			rowData[col++] = totalInfo.accurate;
			rowData[col++] = totalInfo.substitutions;
			rowData[col++] = totalInfo.deleted;

			outputTable.addRow(rowData);
		}
	}

	public boolean isIgnoreDiacritics() {
		return (this.ignoreDiacriticsBox != null ? this.ignoreDiacriticsBox.isSelected(): this.ignoreDiacritics);
	}

	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
		if(this.ignoreDiacriticsBox != null)
			this.ignoreDiacriticsBox.setSelected(ignoreDiacritics);
	}

	public boolean isGlobalAccuracy() {
		return (this.globalAccuracyBox != null ? this.globalAccuracyBox.isSelected() : this.globalAccuracy);
	}

	public void setGlobalAccuracy(boolean globalAccuracy) {
		this.globalAccuracy = globalAccuracy;
		if(this.globalAccuracyBox != null)
			this.globalAccuracyBox.setSelected(globalAccuracy);
	}

	public void setFeatureList(String featureList) {
		this.featureList = featureList;
		if(this.placeAccuracyBox != null) {
			if(featureList.equalsIgnoreCase("PLACE")) {
				this.placeAccuracyBox.setSelected(true);
				featureListField.setText("");
			} else if(featureList.equalsIgnoreCase("MANNER")) {
				this.mannerAccuracyBox.setSelected(true);
				featureListField.setText("");
			} else {
				this.customAccuracyBox.setSelected(true);
				featureListField.setText(featureList);
			}
		}
	}

	public String getFeatureList() {
		if(this.placeAccuracyBox != null) {
			if(this.placeAccuracyBox.isSelected()) {
				return "PLACE";
			} else if(this.mannerAccuracyBox.isSelected()) {
				return "MANNER";
			} else {
				return (featureListField != null ? featureListField.getSelectedText() : this.featureList);
			}
		} else {
			return this.featureList;
		}
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());

			ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
			ignoreDiacriticsBox.setSelected(this.ignoreDiacritics);
			settingsPanel.add(ignoreDiacriticsBox);

			ButtonGroup btnGroup = new ButtonGroup();
			settingsPanel.add(new JXTitledSeparator("Type"));
			globalAccuracyBox = new JRadioButton("Global");
			globalAccuracyBox.setSelected(globalAccuracy);
			btnGroup.add(globalAccuracyBox);
			settingsPanel.add(globalAccuracyBox);

			placeAccuracyBox = new JRadioButton("Place");
			placeAccuracyBox.setSelected(!globalAccuracy && featureList.equalsIgnoreCase("PLACE"));
			btnGroup.add(placeAccuracyBox);
			settingsPanel.add(placeAccuracyBox);

			mannerAccuracyBox = new JRadioButton("Manner");
			mannerAccuracyBox.setSelected(!globalAccuracy && featureList.equalsIgnoreCase("MANNER"));
			btnGroup.add(mannerAccuracyBox);
			settingsPanel.add(mannerAccuracyBox);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();

		retVal.setProperty("ignoreDiacritics", Boolean.toString(isIgnoreDiacritics()));

		retVal.setProperty("globalAccuracy", Boolean.toString(isGlobalAccuracy()));
		retVal.setProperty("featureList", getFeatureList());

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setIgnoreDiacritics(Boolean.parseBoolean(properties.getProperty("ignoreDiacritics", "false")));

		setGlobalAccuracy(Boolean.parseBoolean(properties.getProperty("globalAccuracy", "false")));
		setFeatureList(properties.getProperty("featureList", "PLACE"));
	}

	private class AccuracyInfo {
		int count = 0;
		int accurate = 0;
		int substitutions = 0;
		int deleted = 0;
	}

}
