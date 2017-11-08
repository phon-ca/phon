/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.util.*;
import java.util.stream.Collectors;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.nodes.table.InventorySettings.ColumnInfo;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.IPATranscript;
import ca.phon.query.TableUtils;
import ca.phon.query.report.datasource.*;

@OpNodeInfo(
		name="Inventory",
		description="Aggregated inventory of query results",
		category="Report"
)		
public class InventoryNode extends TableOpNode implements NodeSettings {

	private InventorySettingsPanel settingsPanel = null;
	
	public InventoryNode() {
		super();
		
		final InventorySettings settings = new InventorySettings();
		putExtension(InventorySettings.class, settings);
		putExtension(NodeSettings.class, this);
	}

	public InventorySettings getInventorySettings() {
		return getExtension(InventorySettings.class);
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new InventorySettingsPanel(getInventorySettings());
		}
		return settingsPanel;
	}
	
	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
	}
	
	private String getGroupBy() {
		return (getInventorySettings().getGroupBy() != null ? getInventorySettings().getGroupBy().getName() : null);
	}
	
	private List<String> getColumns() {
		return getInventorySettings().getColumns().stream()
			.map(info -> info.getName()).collect(Collectors.toList());
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource inputTable = (TableDataSource)context.get(tableInput);
		
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		context.put(tableOutput, outputTable);
		
		if(inputTable == null)
			return;
		
		// setup options based on global inputs
		ColumnInfo groupBy = getInventorySettings().getGroupBy();
		if(groupBy == null) {
			groupBy = new ColumnInfo();
			getInventorySettings().setGroupBy(groupBy);
		}
		if(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION)) {
			groupBy.caseSensitive = (boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION);
		}
		if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)) {
			groupBy.ignoreDiacritics = (boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION);
		}
		for(ColumnInfo info:getInventorySettings().getColumns()) {
			if(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION)) {
				info.caseSensitive = (boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION);
			}
			if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)) {
				info.ignoreDiacritics = (boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION);
			}
		}
		
		Set<GroupKey> groupKeys = collectGroupKeys(inputTable);
		
		Map<InventoryRowData, Map<GroupKey, Long>> inventory = 
				generateInventory(groupKeys, inputTable);
		
		final List<String> colNames = getColumns();
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
			outputTable.setColumnTitle(i, colNames.get(i));
		}
	}
	
	private Set<GroupKey> collectGroupKeys(TableDataSource table) {
		Set<GroupKey> retVal = new LinkedHashSet<>();

		if(getGroupBy() != null && getGroupBy().length() > 0) {
			int grouping = getColumnIndex(table, getGroupBy());
			if(grouping >= 0 && grouping < table.getColumnCount()) {
				for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
					retVal.add(new GroupKey(table.getValueAt(rowIdx, grouping)));
				}
			} 
		} else {
			retVal.add(new GroupKey("Total"));
		}
		
		return retVal;
	}
	
	private Map<InventoryRowData, Map<GroupKey, Long>> generateInventory(Set<GroupKey> groupKeys, TableDataSource table) {
		Map<InventoryRowData, Map<GroupKey, Long>> retVal = new LinkedHashMap<>();
		
		int groupingCol = getColumnIndex(table, getGroupBy());
		int[] inventoryCols = getColumnIndices(table, getColumns());
		
		for(int row = 0; row < table.getRowCount(); row++) {
			checkCanceled();
			
			Object grouping = (groupingCol >= 0 
					? table.getValueAt(row, groupingCol) : "Total");
			
			Object[] rowData = new Object[inventoryCols.length];
			for(int ic = 0; ic < inventoryCols.length; ic++) {
				int col = inventoryCols[ic];
				rowData[ic] = (col >= 0 ? 
						table.getValueAt(row, inventoryCols[ic]) : "");
				if(rowData[ic] instanceof IPATranscript && getInventorySettings().getColumns().get(ic).ignoreDiacritics) {
					rowData[ic] = ((IPATranscript)rowData[ic]).removePunctuation().stripDiacritics();
				}
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
	
	private class GroupKey implements Comparable<GroupKey> {
		Object key;
		
		public GroupKey(Object key) {
			this.key = key;
		}
		
		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return TableUtils.checkEquals(key, ((GroupKey)o2).key, 
					getInventorySettings().getGroupBy().caseSensitive,
					getInventorySettings().getGroupBy().ignoreDiacritics);
		}
		
		@Override
		public String toString() {
			return TableUtils.objToString(key, getInventorySettings().getGroupBy().ignoreDiacritics);
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
				final ColumnInfo info = getInventorySettings().getColumns().get(i);
				equals &= TableUtils.checkEquals(rowVal1, rowVal2, info.caseSensitive, info.ignoreDiacritics);
			}
			return equals;
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			for(Object rowVal:rowVals) {
				sb.append((sb.length() > 0 ? "," : ""));
				final  ColumnInfo info = getInventorySettings().getColumns().get(i++);
				sb.append(TableUtils.objToString(rowVal, info.ignoreDiacritics));
			}
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}

}
