package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.FeatureFamily;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.tree.IpaTernaryTree;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.text.PromptedTextField;

@OpNodeInfo(
		name="Phone Accuracy",
		category="Report",
		description="Inventory of IPA Target values with num accurate, substituted, and deleted columns.",
		showInLibrary = true
)
public class PhoneAccuracy extends TableOpNode implements NodeSettings {
	
	private JPanel settingsPanel;
	private ColumnOptionsPanel columnOptionsPanel;
	
	private JRadioButton globalAccuracyBox;
	
	private JRadioButton placeAccuracyBox;
	
	private JRadioButton mannerAccuracyBox;
	
	private JRadioButton customAccuracyBox;
	
	private PromptedTextField featureListField;

	// group by - default 'Session'
	private String groupBy = "Session";
	
	private boolean ignoreDiacritics = false;
	
	// accuracy type
	private boolean globalAccuracy = true;

	// list of features for feature accuracy
	private String featureList = "PLACE";
	
	public PhoneAccuracy() {
		super();
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		context.put(tableInput, outputTable);
		
		if(isGlobalAccuracy()) {
			calculateGlobalAccuracy(context, table, outputTable);
		} else {
			calculateFeatureAccuracy(context, table, outputTable);
		}
	}
	
	private void calculateGlobalAccuracy(OpContext context, DefaultTableDataSource table, DefaultTableDataSource outputTable) {
		IpaTernaryTree<AccuracyInfo> totals = new IpaTernaryTree<>();
		final Map<String, IpaTernaryTree<AccuracyInfo>> 
			accuracyMap = new TreeMap<>();
		
		String groupBy = getGroupBy();
		int groupByCol = (groupBy.length() == 0 ? -1 :
				table.getColumnIndex(groupBy));
		
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
			if(ipaA == null || ipaA.length() == 0) {
				++totalInfo.deleted;
			} else {
				if(ipaT.equals(ipaA)) {
					++totalInfo.accurate;
				} else {
					++totalInfo.substitutions;
				}
			}

			if(groupByCol >= 0) {
				String group = table.getValueAt(row, groupByCol).toString();
			
				IpaTernaryTree<AccuracyInfo> accuracyInfo = accuracyMap.get(group);
				if(accuracyInfo == null) {
					accuracyInfo = new IpaTernaryTree<>();
					accuracyMap.put(group, accuracyInfo);
				}
			
				AccuracyInfo targetInfo = accuracyInfo.get(ipaT);
				if(targetInfo == null) {
					targetInfo = new AccuracyInfo();
					accuracyInfo.put(ipaT, targetInfo);
				}
				++targetInfo.count;
				if(ipaA == null || ipaA.length() == 0) {
					++targetInfo.deleted;
				} else {
					if(ipaT.equals(ipaA)) {
						++targetInfo.accurate;
					} else {
						++targetInfo.substitutions;
					}
				}
			}
		}
		
		// setup columns
		List<String> colNames = new ArrayList<>();
		colNames.add("IPA Target");
		if(groupByCol >= 0) {
			for(String group:accuracyMap.keySet()) {
				colNames.add(group + " : Count");
				colNames.add(group + " : Accurate");
				colNames.add(group + " : Substituted");
				colNames.add(group + " : Deleted");
			}
		}
		colNames.add("Total");
		colNames.add("Total Accurate");
		colNames.add("Total Substituted");
		colNames.add("Total Deleted");
		for(int i = 0; i < colNames.size(); i++) outputTable.setColumnTitle(i, colNames.get(i));
		
		// row data
		for(IPATranscript target:totals.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int col = 0;
			rowData[col++] = target;
			if(groupByCol >= 0) {
				for(String group:accuracyMap.keySet()) {
					IpaTernaryTree<AccuracyInfo> accuracyInfo = accuracyMap.get(group);
					AccuracyInfo targetInfo = accuracyInfo.get(target);
					if(targetInfo == null) {
						targetInfo = new AccuracyInfo();
					}
					rowData[col++] = targetInfo.count;
					rowData[col++] = targetInfo.accurate;
					rowData[col++] = targetInfo.substitutions;
					rowData[col++] = targetInfo.deleted;
				}
			}
			AccuracyInfo totalInfo = totals.get(target);
			rowData[col++] = totalInfo.count;
			rowData[col++] = totalInfo.accurate;
			rowData[col++] = totalInfo.substitutions;
			rowData[col++] = totalInfo.deleted;
			
			outputTable.addRow(rowData);
		}
	}
	
	private void calculateFeatureAccuracy(OpContext context, DefaultTableDataSource table, DefaultTableDataSource outputTable) {
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
		
		String groupBy = getGroupBy();
		int groupByCol = (groupBy.length() == 0 ? -1 :
				table.getColumnIndex(groupBy));
		
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
					ipaT.elementAt(0).getFeatureSet().intersect(fs).getFeatures().stream().findFirst();
			if(!featureNameOpt.isPresent()) continue;
			final String featureName = featureNameOpt.get();
			
			AccuracyInfo totalInfo = totals.get(featureName);
			if(totalInfo == null) {
				totalInfo = new AccuracyInfo();
				totals.put(featureName, totalInfo);
			}
			++totalInfo.count;
			if(ipaA == null || ipaA.length() == 0) {
				++totalInfo.deleted;
			} else {
				if(ipaT.equals(ipaA)) {
					++totalInfo.accurate;
				} else {
					++totalInfo.substitutions;
				}
			}
			
			if(groupByCol >= 0) {
				String group = table.getValueAt(row, groupByCol).toString();
			
				Map<String, AccuracyInfo> accuracyInfo = accuracyMap.get(group);
				if(accuracyInfo == null) {
					accuracyInfo = new TreeMap<>();
					accuracyMap.put(group, accuracyInfo);
				}
			
				AccuracyInfo targetInfo = accuracyInfo.get(featureName);
				if(targetInfo == null) {
					targetInfo = new AccuracyInfo();
					accuracyInfo.put(featureName, targetInfo);
				}
				++targetInfo.count;
				if(ipaA == null || ipaA.length() == 0) {
					++targetInfo.deleted;
				} else {
					if(ipaT.equals(ipaA)) {
						++targetInfo.accurate;
					} else {
						++targetInfo.substitutions;
					}
				}
			}
		}
		
		List<String> colNames = new ArrayList<>();
		colNames.add("Feature");
		if(groupByCol >= 0) {
			for(String group:accuracyMap.keySet()) {
				colNames.add(group + " : Count");
				colNames.add(group + " : Accurate");
				colNames.add(group + " : Substituted");
				colNames.add(group + " : Deleted");
			}
		}
		colNames.add("Total");
		colNames.add("Total Accurate");
		colNames.add("Total Substituted");
		colNames.add("Total Deleted");
		for(int i = 0; i < colNames.size(); i++) outputTable.setColumnTitle(i, colNames.get(i));
		
		for(String feature:totals.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int col = 0;
			rowData[col++] = feature;
			if(groupByCol >= 0) {
				for(String group:accuracyMap.keySet()) {
					Map<String, AccuracyInfo> accuracyInfo = accuracyMap.get(group);
					AccuracyInfo targetInfo = accuracyInfo.get(feature);
					if(targetInfo == null) {
						targetInfo = new AccuracyInfo();
					}
					rowData[col++] = targetInfo.count;
					rowData[col++] = targetInfo.accurate;
					rowData[col++] = targetInfo.substitutions;
					rowData[col++] = targetInfo.deleted;
				}
			}
			AccuracyInfo totalInfo = totals.get(feature);
			rowData[col++] = totalInfo.count;
			rowData[col++] = totalInfo.accurate;
			rowData[col++] = totalInfo.substitutions;
			rowData[col++] = totalInfo.deleted;
			
			outputTable.addRow(rowData);
		}
		
	}
	
	public String getGroupBy() {
		return (this.columnOptionsPanel != null ?
				this.columnOptionsPanel.getColumnNames() : this.groupBy);
	}
	
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
		if(this.columnOptionsPanel != null)
			this.columnOptionsPanel.setColumnNames(groupBy);
	}
	
	public boolean isIgnoreDiacritics() {
		return (this.columnOptionsPanel != null ? this.columnOptionsPanel.isIgnoreDiacritics() : this.ignoreDiacritics);
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
		if(this.columnOptionsPanel != null)
			this.columnOptionsPanel.setIgnoreDiacritics(ignoreDiacritics);
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
			
			columnOptionsPanel = new ColumnOptionsPanel();
			columnOptionsPanel.setShowOptions(true);
			columnOptionsPanel.setColumnNames(groupBy);
			columnOptionsPanel.setIgnoreDiacritics(ignoreDiacritics);
			settingsPanel.add(new JXTitledSeparator("Group By"));
			settingsPanel.add(columnOptionsPanel);
			
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
		
		retVal.setProperty("groupBy", getGroupBy());
		retVal.setProperty("ignoreDiacritics", Boolean.toString(isIgnoreDiacritics()));
		
		retVal.setProperty("globalAccuracy", Boolean.toString(isGlobalAccuracy()));
		retVal.setProperty("featureList", getFeatureList());
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setGroupBy(properties.getProperty("groupBy", "Session"));
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
