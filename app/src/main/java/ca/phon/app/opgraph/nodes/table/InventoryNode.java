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

import ca.phon.app.opgraph.nodes.table.InventorySettings.ColumnInfo;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.TableUtils;
import ca.phon.query.db.*;
import ca.phon.query.report.datasource.*;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculate inventory for a table
 *
 */
@OpNodeInfo(
		name="Inventory",
		description="Aggregated inventory of query results",
		category="Table"
)
public class InventoryNode extends TableOpNode implements NodeSettings {

	private InventorySettings settings = new InventorySettings();
	
	private InventorySettingsPanel settingsPanel = null;

	private InputField ignoreDiacriticsInput = new InputField("ignoreDiacritics", "", true, true, Boolean.class);

	private InputField onlyOrExceptInput = new InputField("onlyOrExcept", "", true, true, Boolean.class);

	private InputField selectedDiacriticsInput = new InputField("selectedDiacritics", "", true, true, Collection.class);

	private OutputField settingsOutput = new OutputField("settings", "Inventory settings", true, InventorySettings.class);

	public InventoryNode() {
		super();

		putField(ignoreDiacriticsInput);
		putField(onlyOrExceptInput);
		putField(selectedDiacriticsInput);

		putField(settingsOutput);
		
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
		final boolean caseSensitive = 
				(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) && !context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default") ? 
						(boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) : settings.isCaseSensitive());
		final String groupingColumn = 
				(context.containsKey(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION) && !context.get(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION).equals("default")
						? context.get(NodeWizard.INVENTORY_GROUPING_GLOBAL_OPTION).toString() : null);
		
		// diacritic options - the following three settings are controled by the ignoreDiacritics flag
		final boolean ignoreDiacritics = 
				(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default") ? 
						(boolean)context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) :
							(context.get(ignoreDiacriticsInput) != null ? (boolean)context.get(ignoreDiacriticsInput) : settings.isIgnoreDiacritics()) );
		
		final boolean onlyOrExcept = 
				(context.containsKey(NodeWizard.ONLYOREXCEPT_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default") ?
						(boolean)context.get(NodeWizard.ONLYOREXCEPT_GLOBAL_OPTION) :
							(context.get(onlyOrExceptInput) != null ? (boolean)context.get(onlyOrExceptInput) : settings.isOnlyOrExcept()) );
		
		@SuppressWarnings("unchecked")
		final Collection<Diacritic> selectedDiacritics = 
				(context.containsKey(NodeWizard.SELECTED_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default") ? 
						((Collection<Diacritic>)context.get(NodeWizard.SELECTED_DIACRITICS_GLOBAL_OPTION)) :
							(context.get(selectedDiacriticsInput) != null ? (Collection<Diacritic>) context.get(selectedDiacriticsInput) : settings.getSelectedDiacritics()) );
		
		if(settings.isAutoGrouping()) {
			ColumnInfo groupBy = new ColumnInfo();
			groupBy.name = (groupingColumn == null ? settings.getAutoGroupingColumn() : groupingColumn);
			groupBy.caseSensitive = caseSensitive;
			groupBy.ignoreDiacritics = ignoreDiacritics;
			groupBy.onlyOrExcept = onlyOrExcept;
			groupBy.selectedDiacritics = selectedDiacritics;
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
			colInfo.onlyOrExcept = onlyOrExcept;
			colInfo.selectedDiacritics = selectedDiacritics;
			colInfo.name = colName;
			settings.addColumn(colInfo);
		}
	}

	@SuppressWarnings("unchecked")
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

		Boolean ignoreDiacriticsOpt =
				(context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")
						? (Boolean) context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)
						: (context.get(ignoreDiacriticsInput) != null ? (Boolean) context.get(ignoreDiacriticsInput) : null));
		if(ignoreDiacriticsOpt != null) {
			groupBy.ignoreDiacritics = ignoreDiacriticsOpt;
		}

		Boolean onlyOrExceptOpt =
				(context.containsKey(NodeWizard.ONLYOREXCEPT_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")
						? (Boolean) context.get(NodeWizard.ONLYOREXCEPT_GLOBAL_OPTION)
						: (context.get(onlyOrExceptInput) != null ? (Boolean) context.get(onlyOrExceptInput) : null));
		if(onlyOrExceptOpt != null) {
			groupBy.onlyOrExcept = onlyOrExceptOpt;
		}

		Collection<Diacritic> selectedDiacriticsOpt =
				(context.containsKey(NodeWizard.SELECTED_DIACRITICS_GLOBAL_OPTION) && !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")
						? (Collection<Diacritic>) context.get(NodeWizard.SELECTED_DIACRITICS_GLOBAL_OPTION)
						: (context.get(selectedDiacriticsInput) != null ? (Collection<Diacritic>) context.get(selectedDiacriticsInput) : null));
		if(selectedDiacriticsOpt != null) {
			groupBy.selectedDiacritics = selectedDiacriticsOpt;
		}

		for(ColumnInfo info:settings.getColumns()) {
			if(context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION) && !context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default")) {
				info.caseSensitive = (boolean)context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION);
			}

			if(ignoreDiacriticsOpt != null) {
				info.ignoreDiacritics = ignoreDiacriticsOpt;
			}

			if(onlyOrExceptOpt != null) {
				info.onlyOrExcept = onlyOrExceptOpt;
			}

			if(selectedDiacriticsOpt != null) {
				info.selectedDiacritics = selectedDiacriticsOpt;
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

		Inventory inventory = generateInventory(settings, groupKeys, inputTable);
		var aggregate = inventory.aggregate;
		var columnSums = inventory.columnSums;

		final List<String> colNames = settings.getColumns().stream().map( (col) -> col.name ).collect(Collectors.toList());
		colNames.removeIf( colname -> inputTable.getColumnIndex(colname) < 0 );
		groupKeys.forEach( key -> colNames.add(key.toString()) );
		settings.getSumColumns().forEach(colNames::add);

		for(InventoryRowData key:aggregate.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int rowDataIdx = 0;
			for(int i = 0; i < key.rowVals.length; i++) {
				String colName = colNames.get(i);
				Optional<ColumnInfo> colInfoOpt = settings.getColumns()
						.stream()
						.filter( (ci) -> ci.getName().equals(colName) )
						.findFirst();

				rowData[rowDataIdx++] = 
						(colInfoOpt.isPresent() && (key.rowVals[i] instanceof String || key.rowVals[i] instanceof IPATranscript)
							?	TableUtils.objToString(key.rowVals[i], colInfoOpt.get().ignoreDiacritics, colInfoOpt.get().onlyOrExcept, colInfoOpt.get().selectedDiacritics)
							:	key.rowVals[i]);
			}
			final Map<GroupKey, Long> count = aggregate.get(key);
			for(GroupKey groupKey:groupKeys) {
				rowData[rowDataIdx++] = count.get(groupKey);
			}
			
			for(String colName:settings.getSumColumns()) {
				rowData[rowDataIdx++] = columnSums.get(key).get(colName);
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
					retVal.add(new GroupKey(table.getValueAt(rowIdx, grouping), settings.getGroupBy().caseSensitive, 
							settings.getGroupBy().ignoreDiacritics, settings.getGroupBy().onlyOrExcept, settings.getGroupBy().selectedDiacritics));
				}
			}
			
			if(!"Session".equals(settings.getGroupBy().name)) {
				List<GroupKey> temp = new ArrayList<>(retVal);
				Collections.sort(temp, (g1, g2) -> TableUtils.compare(g1, g2, settings.getGroupBy().caseSensitive, 
							settings.getGroupBy().ignoreDiacritics, settings.getGroupBy().onlyOrExcept, settings.getGroupBy().selectedDiacritics));
				retVal = new LinkedHashSet<>(temp);
			}
		} else {
			retVal.add(new GroupKey("Total", true, false, false, new HashSet<>()));
		}

		return retVal;
	}

	private Inventory generateInventory(InventorySettings settings, Set<GroupKey> groupKeys, TableDataSource table) {
		Map<InventoryRowData, Map<GroupKey, Long>> aggregate = new LinkedHashMap<>();
		Map<InventoryRowData, Map<String, Number>> columnSums = new LinkedHashMap<>();

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
				if(settings.getColumns().get(ic).ignoreDiacritics) {
					if(rowData[ic] instanceof IPATranscript) {
						IPATranscript val = ((IPATranscript) rowData[ic]).removePunctuation();
						val = (settings.getColumns().get(ic).onlyOrExcept
								? val.stripDiacritics(settings.getColumns().get(ic).selectedDiacritics)
								: val.stripDiacriticsExcept(settings.getColumns().get(ic).selectedDiacritics));
						rowData[ic] = val;
					} else if(rowData[ic] instanceof IPAElement) {
						IPATranscriptBuilder builder = new IPATranscriptBuilder();
						builder.append((IPAElement) rowData[ic]);
						IPATranscript ipa = builder.toIPATranscript();
						if (settings.getColumns().get(ic).onlyOrExcept) {
							ipa = ipa.stripDiacritics(settings.getColumns().get(ic).selectedDiacritics);
						} else {
							ipa = ipa.stripDiacriticsExcept(settings.getColumns().get(ic).selectedDiacritics);
						}
						rowData[ic] = (ipa.length() > 0 ? ipa.elementAt(0) : "");
					} else if(rowData[ic] instanceof Boolean) {
						// do nothing
					} else {
						String txt = (rowData[ic] != null ? FormatterUtil.format(rowData[ic]) : "");
						rowData[ic] = IPATranscript.stripDiacriticsFromText(txt,
								settings.getColumns().get(ic).isOnlyOrExcept(), settings.getColumns().get(ic).getSelectedDiacritics());
					}
				}
			}
			
			final GroupKey groupKey = new GroupKey(grouping, settings.getGroupBy().caseSensitive, 
					settings.getGroupBy().ignoreDiacritics, settings.getGroupBy().onlyOrExcept, settings.getGroupBy().selectedDiacritics);
			final InventoryRowData key = new InventoryRowData(settings, rowData);
			Map<GroupKey, Long> counts = aggregate.get(key);
			if(counts == null) {
				counts = new LinkedHashMap<>();
				for(GroupKey gk:groupKeys) counts.put(gk, 0L);
				aggregate.put(key, counts);
			}
			long count = counts.get(groupKey);
			counts.put(groupKey, ++count);
			
			Map<String, Number> sums = columnSums.get(key);
			if(sums == null) {
				sums = new LinkedHashMap<String, Number>();
				for(String colName:settings.getSumColumns()) sums.put(colName, Integer.valueOf(0));
				columnSums.put(key, sums);
			}
			for(String sumColumn:settings.getSumColumns()) {
				int colIdx = table.getColumnIndex(sumColumn);
				if(colIdx < 0) continue;
				
				Object rowValue = table.getValueAt(row, colIdx);
				Number rowNum = Integer.valueOf(0);
				
				if(rowValue != null) {
					if(rowValue instanceof Number) {
						rowNum = (Number)rowValue;
					}
					String rowValueText = rowValue.toString();
					try {
						rowNum = Integer.parseInt(rowValueText);
					} catch (NumberFormatException e1) {
						try {
							rowNum = Double.parseDouble(rowValueText);
						} catch (NumberFormatException e2) {}
					}
				}
				
				Number currentValue = sums.get(sumColumn);
				
				// keep as integer if possible...
				if(currentValue instanceof Integer 
						&& rowValue instanceof Integer) {
					int sum = currentValue.intValue() + rowNum.intValue();
					sums.put(sumColumn, sum);
				} else {
					// switch to doubles
					double sum = currentValue.doubleValue() + rowNum.doubleValue();
					sums.put(sumColumn, sum);
				}
			}
		}

		Inventory retVal = new Inventory();
		retVal.aggregate = aggregate;
		retVal.columnSums = columnSums;
		
		return retVal;
	}

	private class Inventory {
		
		Map<InventoryRowData, Map<GroupKey, Long>> aggregate;
		
		Map<InventoryRowData, Map<String, Number>> columnSums;
		
	}
	
	private class GroupKey implements Comparable<GroupKey> {
		Object key;

		boolean caseSensitive;
		
		boolean ignoreDiacritics;
		
		boolean onlyOrExcept;
		
		Collection<Diacritic> selectedDiacritics;
		
		public GroupKey(Object key, boolean caseSensitive, boolean ignoreDiacritics, boolean onlyOrExcept, Collection<Diacritic> selectedDiacritics) {
			this.key = key;
			this.caseSensitive = caseSensitive;
			this.ignoreDiacritics = ignoreDiacritics;
			this.onlyOrExcept = onlyOrExcept;
			this.selectedDiacritics = selectedDiacritics;
		}

		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return TableUtils.checkEquals(key, ((GroupKey)o2).key,
					caseSensitive,
					ignoreDiacritics,
					onlyOrExcept,
					selectedDiacritics);
		}

		@Override
		public String toString() {
			return TableUtils.objToString(key, ignoreDiacritics, onlyOrExcept, selectedDiacritics);
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
		
		Map<String, Number> columnSums;

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
				equals &= TableUtils.checkEquals(rowVal1, rowVal2, info.caseSensitive, info.ignoreDiacritics, info.onlyOrExcept, info.selectedDiacritics);
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
				sb.append(TableUtils.objToString(rowVal, info.ignoreDiacritics, info.onlyOrExcept, info.selectedDiacritics));
			}
			return sb.toString();
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
	}

}
