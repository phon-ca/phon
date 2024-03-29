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

import ca.phon.ipa.Diacritic;

import java.util.*;

public class InventorySettings implements Cloneable, IgnoreDiacriticsSettings {
	
	private boolean configureAutomatically = true;
	
	private boolean caseSensitive = true;
	
	private boolean ignoreDiacritics = false;
	
	/*
	 * Only = true
	 * Except = false
	 */
	private boolean onlyOrExcept = false;
	
	private Collection<Diacritic> selectedDiacritics = new ArrayList<>();
	
	private boolean autoGrouping = true;
	
	private String autoGroupingColumn = "Session";
	
	private boolean includeMetadata = true;
	
	private boolean includeAdditionalGroupData = false;
	
	private boolean includeAdditionalWordData = false;

	private ColumnInfo groupBy;
	
	private List<ColumnInfo> columns;
	
	private List<String> sumColumns;
	
	public InventorySettings() {
		super();
		
		columns = new ArrayList<>();
		sumColumns = new ArrayList<>();
	}
	
	public boolean isAutoGrouping() {
		return autoGrouping;
	}

	public void setAutoGrouping(boolean autoGrouping) {
		this.autoGrouping = autoGrouping;
	}

	public String getAutoGroupingColumn() {
		return autoGroupingColumn;
	}

	public void setAutoGroupingColumn(String autoGroupingColumn) {
		this.autoGroupingColumn = autoGroupingColumn;
	}

	public boolean isConfigureAutomatically() {
		return configureAutomatically;
	}

	public void setConfigureAutomatically(boolean configureAutomatically) {
		this.configureAutomatically = configureAutomatically;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isIgnoreDiacritics() {
		return ignoreDiacritics;
	}

	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
	}
	
	/**
	 * Selection mode when ignoring diacritics.  If <code>true</code>
	 * selection mode will be 'Only' if <code>false</code> selection mode
	 * will b 'Except'.  This setting is related to the selected diacritics.
	 * 
	 * @return diacritic filtering mode
	 */
	public boolean isOnlyOrExcept() {
		return this.onlyOrExcept;
	}
	
	public void setOnlyOrExcept(boolean onlyOrExcept) {
		this.onlyOrExcept = onlyOrExcept;
	}
	
	/**
	 * Diacritics selected for filtering.  The behaviour is determined
	 * by the onlyOrExcept paramter.
	 * 
	 * @return selected diacritics
	 */
	public Collection<Diacritic> getSelectedDiacritics() {
		return this.selectedDiacritics;
	}
	
	public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics) {
		this.selectedDiacritics = selectedDiacritics;
	}

	public boolean isIncludeMetadata() {
		return includeMetadata;
	}

	public void setIncludeMetadata(boolean includeMetadata) {
		this.includeMetadata = includeMetadata;
	}

	public boolean isIncludeAdditionalGroupData() {
		return includeAdditionalGroupData;
	}

	public void setIncludeAdditionalGroupData(boolean includeAdditionalGroupData) {
		this.includeAdditionalGroupData = includeAdditionalGroupData;
	}

	public boolean isIncludeAdditionalWordData() {
		return includeAdditionalWordData;
	}

	public void setIncludeAdditionalWordData(boolean includeAdditionalWordData) {
		this.includeAdditionalWordData = includeAdditionalWordData;
	}

	public ColumnInfo getGroupBy() {
		return this.groupBy;
	}
	
	public void setGroupBy(ColumnInfo info) {
		this.groupBy = info;
	}
	
	public List<ColumnInfo> getColumns() {
		return this.columns;
	}
	
	public void clearColumns() {
		this.columns.clear();
	}
	
	public void removeColumn(ColumnInfo info) {
		this.columns.remove(info);
	}
	
	public void removeColumn(int idx) {
		this.columns.remove(idx);
	}
	
	public ColumnInfo addColumn(String col, boolean caseSensitive, boolean ignoreDiacritics) {
		ColumnInfo info = new ColumnInfo();
		info.name = col;
		info.caseSensitive = caseSensitive;
		info.ignoreDiacritics = ignoreDiacritics;
		addColumn(info);
		return info;
	}
	
	public void addColumn(ColumnInfo info) {
		columns.add(info);
	}
	
	public List<String> getSumColumns() {
		return this.sumColumns;
	}
	
	public void clearSumColumns() {
		this.sumColumns.clear();
	}
	
	public boolean removeSumColumn(String col) {
		return this.sumColumns.remove(col);
	}
	
	public String removeSumColumn(int col) {
		return this.sumColumns.remove(col);
	}
	
	public void addSumColumn(String sumColumn) {
		this.sumColumns.add(sumColumn);
	}
	
	public static class ColumnInfo implements IgnoreDiacriticsSettings {
		String name = "";
		boolean caseSensitive = true;
		
		// diacritic options
		boolean ignoreDiacritics = false;
		boolean onlyOrExcept = false;
		Collection<Diacritic> selectedDiacritics = new ArrayList<Diacritic>();
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isCaseSensitive() {
			return caseSensitive;
		}
		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
		}
	
		public boolean isIgnoreDiacritics() {
			return ignoreDiacritics;
		}
		public void setIgnoreDiacritics(boolean ignoreDiacritics) {
			this.ignoreDiacritics = ignoreDiacritics;
		}
	
		public boolean isOnlyOrExcept() {
			return this.onlyOrExcept;
		}
		public void setOnlyOrExcept(boolean onlyOrExcept) {
			this.onlyOrExcept = onlyOrExcept;
		}
		
		public Collection<Diacritic> getSelectedDiacritics() {
			return this.selectedDiacritics;
		}
		public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics) {
			this.selectedDiacritics = selectedDiacritics;
		}
	}
	
	@Override
	public Object clone() {
		InventorySettings retVal = new InventorySettings();
		
		retVal.setConfigureAutomatically(isConfigureAutomatically());
		retVal.setAutoGrouping(isAutoGrouping());
		retVal.setAutoGroupingColumn(getAutoGroupingColumn());
		retVal.setIncludeMetadata(isIncludeMetadata());
		retVal.setIncludeAdditionalGroupData(isIncludeAdditionalGroupData());
		retVal.setIncludeAdditionalWordData(isIncludeAdditionalWordData());
		retVal.setCaseSensitive(isCaseSensitive());
		retVal.setIgnoreDiacritics(isIgnoreDiacritics());
		retVal.setOnlyOrExcept(isOnlyOrExcept());
		retVal.setSelectedDiacritics(getSelectedDiacritics());
				
		if(getGroupBy() != null) {
			ColumnInfo groupBy = new ColumnInfo();
			groupBy.caseSensitive = getGroupBy().caseSensitive;
			groupBy.ignoreDiacritics = getGroupBy().ignoreDiacritics;
			groupBy.onlyOrExcept = getGroupBy().onlyOrExcept;
			groupBy.selectedDiacritics = getGroupBy().selectedDiacritics;
			groupBy.name = getGroupBy().name;
			
			retVal.setGroupBy(groupBy);
		}
		
		for(ColumnInfo columnInfo:getColumns()) {
			ColumnInfo colInfo = new ColumnInfo();
			colInfo.caseSensitive = columnInfo.caseSensitive;
			colInfo.ignoreDiacritics = columnInfo.ignoreDiacritics;
			colInfo.onlyOrExcept = columnInfo.onlyOrExcept;
			colInfo.selectedDiacritics = columnInfo.selectedDiacritics;
			colInfo.name = columnInfo.name;
			retVal.addColumn(colInfo);
		}
		
		retVal.sumColumns.addAll(this.sumColumns);
		
		return retVal;
	}
	
}
