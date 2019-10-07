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
package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import ca.phon.app.opgraph.nodes.table.InventorySettings.ColumnInfo;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.TableUtils;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultValue;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Inventory",
		description="Aggregated inventory of query results",
		category="Table"
)
public class InventoryNode extends TableOpNode implements NodeSettings {

	private InventorySettingsPanel settingsPanel = null;
	
	private OutputField settingsOutput = new OutputField("settings", "Inventory settings", true, InventorySettings.class);

	public InventoryNode() {
		super();

		putField(settingsOutput);
		
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
	
	private void automaticConfiguration(InventorySettings settings, OpContext context, Result result) {
		final boolean ignoreDiacritics = 
				(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default") ? 
						(boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) : settings.isIgnoreDiacritics());
		final boolean caseSensitive = 
				(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) && !context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default") ? 
						(boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) : settings.isCaseSensitive());
		final String groupingColumn = 
				(context.containsKey(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION) && !context.get(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION).equals("default")
						? context.get(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION).toString() : null);
		
		if(settings.isAutoGrouping()) {
			ColumnInfo groupBy = new ColumnInfo();
			groupBy.name = (groupingColumn == null ? settings.getAutoGroupingColumn() : groupingColumn);
			groupBy.caseSensitive = caseSensitive;
			groupBy.ignoreDiacritics = ignoreDiacritics;
			settings.setGroupBy(groupBy);
		}
		
		// clear previous auto config settings if they exist
		settings.clearColumns();
		
		Set<String> colNames = new LinkedHashSet<>();
		
		// result values first
		for(ResultValue rv:result) {
			final String colName = rv.getName();
			
			if(colName.equals(rv.getTierName())) {
				// primary result value, always include
				colNames.add(colName);
			} else if(colName.equals(rv.getTierName() + " (Group)") 
					&& settings.isIncludeAdditionalGroupData()) {
				colNames.add(colName);
			} else if(colName.equals(rv.getTierName() + " (Word)")
					&& settings.isIncludeAdditionalWordData()) {
				colNames.add(colName);
			}
		}
		if(settings.isIncludeMetadata()) {
			colNames.addAll(result.getMetadata().keySet());
		}
		
		for(String colName:colNames) {
			ColumnInfo colInfo = new ColumnInfo();
			colInfo.caseSensitive = caseSensitive;
			colInfo.ignoreDiacritics = ignoreDiacritics;
			colInfo.name = colName;
			settings.addColumn(colInfo);
		}
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource inputTable = (TableDataSource)context.get(tableInput);

		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		context.put(tableOutput, outputTable);

		if(inputTable == null)
			return;
		
		final InventorySettings settings = (InventorySettings)getInventorySettings().clone();
		// auto config if necessary
		if(settings.isConfigureAutomatically()) {
			int resultColIdx = inputTable.getColumnIndex("Result");
			if(resultColIdx < 0) 
				throw new ProcessingException(null, "Unable to automatically configure columns; 'Result' column not found in input table.");
			
			if(inputTable.getRowCount() > 0) {
				Result r = (Result)inputTable.getValueAt(0, resultColIdx);
				automaticConfiguration(settings, context, r);
			}
		}
		context.put(settingsOutput, settings);
		
		
		// setup options based on global inputs
		ColumnInfo groupBy = settings.getGroupBy();
		if(groupBy == null) {
			groupBy = new ColumnInfo();
			settings.setGroupBy(groupBy);
		}
		if(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) && !context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default")) {
			groupBy.caseSensitive = (boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION);
		}
		if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")) {
			groupBy.ignoreDiacritics = (boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION);
			groupBy.retainDiacritics = (Set<Diacritic>)context.get(NodeWizard.RETAIN_DIACRITICS_GLOBAL_OPTION);
		}
		for(ColumnInfo info:settings.getColumns()) {
			if(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) && !context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default")) {
				info.caseSensitive = (boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION);
			}
			if(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")) {
				info.ignoreDiacritics = (boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION);
				info.retainDiacritics = (Set<Diacritic>)context.get(NodeWizard.RETAIN_DIACRITICS_GLOBAL_OPTION);
			}
		}
		
		if(settings.getGroupBy() != null && settings.getGroupBy().name != null) {
			final String groupByColumn = settings.getGroupBy().name;
			if(context.containsKey(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION) && !context.get(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION).equals("default") 
				 &&	(groupByColumn.equals("Session") || groupByColumn.equals("Age")) ) {
				settings.getGroupBy().name = context.get(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION).toString();
			}
		}

		Set<GroupKey> groupKeys = collectGroupKeys(settings, inputTable);

		Map<InventoryRowData, Map<GroupKey, Long>> inventory =
				generateInventory(settings, groupKeys, inputTable);

		final List<String> colNames = settings.getColumns().stream().map( (col) -> col.name ).collect(Collectors.toList());
		colNames.removeIf( colname -> inputTable.getColumnIndex(colname) < 0 );
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

	private Set<GroupKey> collectGroupKeys(InventorySettings settings, TableDataSource table) {
		Set<GroupKey> retVal = new LinkedHashSet<>();

		if(settings.getGroupBy() != null && settings.getGroupBy().name.length() > 0) {
			int grouping = getColumnIndex(table, settings.getGroupBy().name);
			if(grouping >= 0 && grouping < table.getColumnCount()) {
				for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
					retVal.add(new GroupKey(table.getValueAt(rowIdx, grouping), settings.getGroupBy().caseSensitive, settings.getGroupBy().ignoreDiacritics));
				}
			}
		} else {
			retVal.add(new GroupKey("Total", true, false));
		}

		return retVal;
	}

	private Map<InventoryRowData, Map<GroupKey, Long>> generateInventory(InventorySettings settings, Set<GroupKey> groupKeys, TableDataSource table) {
		Map<InventoryRowData, Map<GroupKey, Long>> retVal = new LinkedHashMap<>();

		int groupingCol = getColumnIndex(table, settings.getGroupBy().name);
		final List<String> columns = settings.getColumns().stream().map( (col) -> col.name ).collect(Collectors.toList());
		int[] inventoryCols = getColumnIndices(table, columns);

		for(int row = 0; row < table.getRowCount(); row++) {
			checkCanceled();

			Object grouping = (groupingCol >= 0
					? table.getValueAt(row, groupingCol) : "Total");

			Object[] rowData = new Object[inventoryCols.length];
			for(int ic = 0; ic < inventoryCols.length; ic++) {
				int col = inventoryCols[ic];
				rowData[ic] = (col >= 0 ?
						table.getValueAt(row, inventoryCols[ic]) : "");
				if(rowData[ic] instanceof IPATranscript && settings.getColumns().get(ic).ignoreDiacritics) {
					rowData[ic] = ((IPATranscript)rowData[ic]).removePunctuation().stripDiacritics();
				}
			}

			final GroupKey groupKey = new GroupKey(grouping, settings.getGroupBy().caseSensitive, settings.getGroupBy().ignoreDiacritics);
			final InventoryRowData key = new InventoryRowData(settings, rowData);
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

		boolean caseSensitive;
		
		boolean ignoreDiacritics;
		
		public GroupKey(Object key, boolean caseSensitive, boolean ignoreDiacritics) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return TableUtils.checkEquals(key, ((GroupKey)o2).key,
					caseSensitive,
					ignoreDiacritics);
		}

		@Override
		public String toString() {
			return TableUtils.objToString(key, ignoreDiacritics);
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
		
		InventorySettings settings;

		public InventoryRowData(InventorySettings settings, Object[] vals) {
			this.settings = settings;
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
				final ColumnInfo info = settings.getColumns().get(i);
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
				final  ColumnInfo info = settings.getColumns().get(i++);
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
