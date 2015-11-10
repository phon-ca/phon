package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipa.IPATranscript;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Inventory",
		description="Aggregated inventory of query results",
		category="Report"
)		
public class InventoryNode extends TableOpNode implements NodeSettings {

	private InventorySettingsPanel settingsPanel = null;
	
	private String groupBy = "Session";
	
	private String columns = "Result";
	
	private boolean caseSensitive = false;
	
	private boolean ignoreDiacritics = true;
	
	public InventoryNode() {
		super();
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new InventorySettingsPanel();
			
			settingsPanel.setGroupingBy(groupBy);
			settingsPanel.setColumns(columns);
			settingsPanel.setCaseSensitive(caseSensitive);
			settingsPanel.setIgnoreDiacritics(ignoreDiacritics);
		}
		return settingsPanel;
	}
	
	public boolean isCaseSensitive() {
		return (settingsPanel != null ? settingsPanel.isCaseSensitive() : caseSensitive);
	}
	
	public boolean isIgnoreDiacritics() {
		return (settingsPanel != null ? settingsPanel.isIgnoreDiacritics() : ignoreDiacritics);
	}
	
	public String getGroupBy() {
		return (settingsPanel != null ? settingsPanel.getGroupingBy() : groupBy);
	}
	
	public String getColumns() {
		return (settingsPanel != null ? settingsPanel.getColumns() : columns);
	}
	
	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		props.put("groupBy", getGroupBy());
		props.put("columns", getColumns());
		props.put("caseSensitive", isCaseSensitive());
		props.put("ignoreDiacritics", isIgnoreDiacritics());
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		groupBy = properties.getProperty("groupBy");
		columns = properties.getProperty("columns");
		caseSensitive = Boolean.parseBoolean(properties.getProperty("caseSensitive", "false"));
		ignoreDiacritics = Boolean.parseBoolean(properties.getProperty("ignoreDiacritics", "true"));
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource inputTable = (TableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();

		Set<GroupKey> groupKeys = collectGroupKeys(inputTable);
		
		Map<InventoryRowData, Map<GroupKey, Long>> inventory = 
				generateInventory(groupKeys, inputTable);
		
		int[] inventoryCols = getColumnIndices(inputTable, getColumns());
		
		List<String> colNames = new ArrayList<>();
		Arrays.stream(inventoryCols).forEach( col -> colNames.add(inputTable.getColumnTitle(col)) );
		groupKeys.forEach( key -> colNames.add(key.toString()) );
		
		for(InventoryRowData key:inventory.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int rowDataIdx = 0;
			for(int i = 0; i < key.rowVals.length; i++) {
				rowData[rowDataIdx++] = key.rowVals[i];
			}
			final Map<GroupKey, Long> count = inventory.get(key);
			for(GroupKey groupKey:groupKeys) {
				rowData[rowDataIdx++] = count.get(groupKey);
			}
			
			outputTable.addRow(rowData);
		}
		
		for(int i = 0; i < colNames.size(); i++) {
			outputTable.setColumntTitle(i, colNames.get(i));
		}
		
		context.put(tableOutput, outputTable);
	}
	
	private Set<GroupKey> collectGroupKeys(TableDataSource table) {
		Set<GroupKey> retVal = new LinkedHashSet<>();
		
		int[] grouping = getColumnIndices(table, getGroupBy());
		if(grouping.length > 0 && grouping[0] >= 0 && grouping[0] < table.getColumnCount()) {
			for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
				retVal.add(new GroupKey(table.getValueAt(rowIdx, grouping[0])));
			}
		} else {
			retVal.add(new GroupKey("Total"));
		}
		
		return retVal;
	}
	
	private Map<InventoryRowData, Map<GroupKey, Long>> generateInventory(Set<GroupKey> groupKeys, TableDataSource table) {
		Map<InventoryRowData, Map<GroupKey, Long>> retVal = new LinkedHashMap<>();
		
		int[] groupingCols = getColumnIndices(table, getGroupBy());
		int[] inventoryCols = getColumnIndices(table, getColumns());
		
		for(int row = 0; row < table.getRowCount(); row++) {
			Object grouping = (groupingCols.length > 0 && groupingCols[0] >= 0 
					? table.getValueAt(row, groupingCols[0]) : "Total");
			
			Object[] rowData = new Object[inventoryCols.length];
			for(int ic = 0; ic < inventoryCols.length; ic++) {
				int col = inventoryCols[ic];
				rowData[ic] = (col >= 0 ? 
						table.getValueAt(row, inventoryCols[ic]) : "");
			}
			
			final GroupKey groupKey = new GroupKey(grouping);
			final InventoryRowData key = new InventoryRowData(rowData);
			Map<GroupKey, Long> counts = retVal.get(key);
			if(counts == null) {
				counts = new LinkedHashMap<>();
				for(GroupKey gk:groupKeys) counts.put(gk, 0L);
				retVal.put(key, counts);
			}
			long count = counts.get(groupKey);
			counts.put(groupKey, ++count);
		}
		
		return retVal;
	}
	
	private boolean checkEquals(Object o1, Object o2) {
		if(o1 == null && o2 != null) return false;
		else if(o1 == null && o2 == o1) return true;
		
		final Class<?> type = o1.getClass();
		@SuppressWarnings("unchecked")
		final Formatter<Object> formatter = 
				(Formatter<Object>)FormatterFactory.createFormatter(type);
		
		String o1Txt = (formatter != null ? formatter.format(o1) : o1.toString());
		String o2Txt = (formatter != null ? formatter.format(o2) : o2.toString());
		
		if(isIgnoreDiacritics()) {
			try {
				final IPATranscript ipa = IPATranscript.parseIPATranscript(o1Txt);
				o1Txt = ipa.removePunctuation().stripDiacritics().toString();
				
				final IPATranscript ipa2 = IPATranscript.parseIPATranscript(o2Txt);
				o2Txt = ipa2.removePunctuation().stripDiacritics().toString();
			} catch (ParseException e) {}
		}
		
		return (isCaseSensitive() ? o1Txt.equals(o2Txt) : o1Txt.equalsIgnoreCase(o2Txt));
	}
	
	private String objToString(Object val) {
		String retVal = (val != null ? val.toString() : "");
		if(isIgnoreDiacritics()) {
			try {
				IPATranscript transcript = (val instanceof IPATranscript ? (IPATranscript) val :
					IPATranscript.parseIPATranscript(retVal));
				retVal = transcript.removePunctuation().stripDiacritics().toString();
			} catch (ParseException e) {
				
			}
		}
		return retVal;
	}
	
	private class GroupKey implements Comparable<GroupKey> {
		Object key;
		
		public GroupKey(Object key) {
			this.key = key;
		}
		
		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return checkEquals(key, ((GroupKey)o2).key);
		}
		
		@Override
		public String toString() {
			return objToString(key);
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public int compareTo(GroupKey k2) {
			return toString().compareTo(k2.toString());
		}
		
	}
	
	private class InventoryRowData {
		Object[] rowVals;
		
		public InventoryRowData(Object[] vals) {
			this.rowVals = vals;
		}
		
		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof InventoryRowData)) return false;
			
			final InventoryRowData otherRow = (InventoryRowData)o2;
			if(otherRow == this) return true;
			
			if(otherRow.rowVals.length != rowVals.length) return false;
			
			boolean equals = true;
			for(int i = 0; i < rowVals.length; i++) {
				Object rowVal1 = rowVals[i];
				Object rowVal2 = otherRow.rowVals[i];
				equals &= checkEquals(rowVal1, rowVal2);
			}
			return equals;
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(Object rowVal:rowVals) {
				sb.append((sb.length() > 0 ? "," : ""));
				sb.append(objToString(rowVal));
			}
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}

}
