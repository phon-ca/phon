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
package ca.phon.query.report.datasource;

import ca.phon.query.TableUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Basic implementation of a {@link TableDataSource}.  Data
 * is stored in a list of arrays.
 *
 */
public class DefaultTableDataSource implements TableDataSource {

	private String[] columnNames = new String[0];

	private List<Object[]> rowData = new ArrayList<Object[]>();

	public DefaultTableDataSource() {
		super();
	}

	public DefaultTableDataSource(DefaultTableDataSource from) {
		super();
		rowData.addAll(from.rowData);
	}

	public List<Object[]> getRowData() {
		return this.rowData;
	}

	public Object[] getRow(int row) {
		return rowData.get(row);
	}

	public void deleteRow(int row) {
		rowData.remove(row);
	}

	public void insertRow(int idx, Object[] row) {
		if(row.length < getColumnCount()) {
			row = Arrays.copyOf(row, getColumnCount());
		} else if(row.length > getColumnCount()) {
			setColumnCount(row.length);
		}

		rowData.add(idx, row);
	}

	public void addRow(Object[] row) {
		if(row.length < getColumnCount()) {
			row = Arrays.copyOf(row, getColumnCount());
		} else if(row.length > getColumnCount()) {
			setColumnCount(row.length);
		}

		rowData.add(row);
	}

	protected void setColumnCount(int columns) {
		if(columns == getColumnCount()) return;

		columnNames = Arrays.copyOf(columnNames, columns);

		List<Object[]> newRowData = new ArrayList<>();
		rowData.forEach( (row) -> newRowData.add(Arrays.copyOf(row, columns)) );
		rowData = newRowData;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return rowData.size();
	}

	@Override
	public int getColumnIndex(String columnName) {
		int colIdx = -1;
		for(int c = 0; c < getColumnCount(); c++) {
			if(getColumnTitle(c).equals(columnName)) {
				colIdx = c;
				break;
			}
		}
		return colIdx;
	}
	
	/**
	 * Get all rows which match the given key values
	 * for the given key columns.
	 * 
	 * @param keycols
	 * @param keyvals
	 * @return list of rows which match
	 */
	public List<Object[]> findRows(int keyCols[], String[] rowKey) {
		return findRows(keyCols, rowKey, false, true);
	}
	
	public List<Object[]> findRows(int keyCols[], String[] rowKey, boolean ignoreDiacritics, boolean caseSensitive) {
		return rowData.parallelStream()
			.filter( r -> {
				boolean equals = true;
				for(int i = 0; i < keyCols.length && equals; i++) {
					String testVal = TableUtils.objToString(r[keyCols[i]], ignoreDiacritics);
					equals &=
							(caseSensitive ? rowKey[i].equals(testVal) : rowKey[i].equalsIgnoreCase(testVal));
				}
				return equals;
			} ).collect(Collectors.toList());
	}

	public Object getValueAt(int row, String columnName) {
		Object retVal = null;

		if(row < rowData.size()) {
			int colIdx = getColumnIndex(columnName);
			if(colIdx >= 0 && colIdx < getColumnCount()) {
				retVal = getValueAt(row, colIdx);
			}
		}

		return retVal;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = null;

		if(row < rowData.size()) {
			final Object[] rowVal = rowData.get(row);
			if(col < rowVal.length) {
				retVal = rowVal[col];
			}
		}

		return retVal;
	}

	/**
	 * Return the value at cell for given rowKey, colName.
	 *
	 * This method assumes that each row begins with a unique
	 * key.
	 *
	 * @param rowKey
	 * @param colName
	 * @return cellValue at given rowKey, colName intersection.
	 */
	public Object getValueAt(String rowKey, String colName) {
		return getValueAt(0, rowKey, colName);
	}

	public Object getValueAt(String keyCol, String rowKey, String colName) {
		return getValueAt(getColumnIndex(keyCol), rowKey, colName);
	}

	public Object getValueAt(int keyCol, String rowKey, String colName) {
		return getValueAt(new int[] {keyCol}, new String[] {rowKey}, colName);
	}

	public Object getValueAt(final int[] keyCols, final String[] rowKey, String colName) {
		return getValueAt(keyCols, rowKey, colName, false, true);
	}

	public Object getValueAt(final int[] keyCols, final String[] rowKey, String colName, boolean ignoreDiacritics, boolean caseSensitive) {
		Optional<Object[]> row =
				rowData.parallelStream()
					.filter( r -> {
						boolean equals = true;
						for(int i = 0; i < keyCols.length && equals; i++) {
							String testVal = TableUtils.objToString(r[keyCols[i]], ignoreDiacritics);
							equals &=
									(caseSensitive ? rowKey[i].equals(testVal) : rowKey[i].equalsIgnoreCase(testVal));
						}
						return equals;
					} )
					.findAny();
		final int colIdx = getColumnIndex(colName);
		if(row.isPresent() && colIdx >= 0) {
			return row.get()[colIdx];
		} else {
			return null;
		}
	}

	public void setValueAt(final int[] keyCols, final String[] rowKey, String colName, Object value) {
		setValueAt(keyCols, rowKey, colName, false, true, value);
	}

	public void setValueAt(final int[] keyCols, final String[] rowKey, String colName, boolean ignoreDiacritics, boolean caseSensitive, Object value) {
		Optional<Object[]> row =
				rowData.parallelStream()
					.filter( r -> {
						boolean equals = true;
						for(int i = 0; i < keyCols.length && equals; i++) {
							String testVal = TableUtils.objToString(r[keyCols[i]], ignoreDiacritics);
							equals &=
									(caseSensitive ? rowKey[i].equals(testVal) : rowKey[i].equalsIgnoreCase(testVal));
						}
						return equals;
					} )
					.findAny();
		final int colIdx = getColumnIndex(colName);
		if(row.isPresent() && colIdx >= 0) {
			row.get()[colIdx] = value;
		}
	}

	public void setValueAt(int row, int col, Object newVal) {
		final Object[] rowVals = getRow(row);
		rowVals[col] = newVal;
	}

	public void setValueAt(int row, String colname, Object newVal) {
		int columnIndex = getColumnIndex(colname);
		setValueAt(row, columnIndex, newVal);
	}

	public String getDefaultColumnTitle(int col) {
		int let1 = col / 26;
		int let2 = col % 26;

		String retVal = "" +
				(let1 > 0 ? (char)('A' + (let1-1)) : "") +
				(char)('A' + let2);
		return retVal;
	}

	@Override
	public String getColumnTitle(int col) {
		String retVal = null;

		if(col >= 0 && col < columnNames.length) {
			retVal =
				(columnNames[col] != null ? columnNames[col] : getDefaultColumnTitle(col));
		}

		return retVal;
	}

	public void setColumnTitle(int col, String title) {
		if(col >= getColumnCount()) {
			setColumnCount(col+1);
		}
		columnNames[col] = title;
	}

	/**
	 * Return the type of the first non-null value encountered
	 * in the specified column.
	 *
	 * @param col
	 *
	 * @return class of the first real value in the columm
	 */
	public Class<?> inferColumnType(int col) {
		Class<?> retVal = Object.class;

		for(int row = 0; row < getRowCount(); row++) {
			Object val = getValueAt(row, col);
			if(val != null) {
				retVal = val.getClass();
				break;
			}
		}

		return retVal;
	}

	public void append(DefaultTableDataSource otherTable) {
		rowData.addAll(otherTable.rowData);
	}

}
