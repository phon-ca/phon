package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.tree.IpaTernaryTree;
import ca.phon.query.report.datasource.DefaultTableDataSource;

@OpNodeInfo(
		name="Phone Accuracy",
		category="Report",
		description="Inventory of IPA Target values with num accurate, substituted, and deleted columns.",
		showInLibrary = true
)
public class PhoneAccuracy extends TableOpNode implements NodeSettings {
	
	private JPanel settingsPanel;
	private ColumnOptionsPanel columnOptionsPanel;

	// group by - default 'Session'
	private String groupBy = "Session";
	
	private boolean ignoreDiacritics = false;
	
	public PhoneAccuracy() {
		super();
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		context.put(tableInput, outputTable);
		
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

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout());
			
			columnOptionsPanel = new ColumnOptionsPanel();
			columnOptionsPanel.setShowOptions(true);
			settingsPanel.add(new JXTitledSeparator("Group By"));
			settingsPanel.add(columnOptionsPanel);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		
		retVal.setProperty("groupBy", getGroupBy());
		retVal.setProperty("ignoreDiacritics", Boolean.toString(isIgnoreDiacritics()));
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setGroupBy(properties.getProperty("groupBy", "Session"));
		setIgnoreDiacritics(Boolean.parseBoolean(properties.getProperty("ignoreDiacritics", "false")));
	}
	
	private class AccuracyInfo {
		int count = 0;
		int accurate = 0;
		int substitutions = 0;
		int deleted = 0;
	}

}
