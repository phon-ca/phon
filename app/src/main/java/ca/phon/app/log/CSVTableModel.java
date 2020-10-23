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
package ca.phon.app.log;

import java.io.*;
import java.util.*;

import javax.swing.table.*;

import au.com.bytecode.opencsv.*;

/**
 * Table model using a CSV reader for table data.
 */
public class CSVTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 5228655036751277639L;

	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(CSVTableModel.class.getName());

	private boolean useFirstRowAsHeader = true;
	
	private int maxCols = -1;
	
	private List<String[]> data = new ArrayList<String[]>();
	
	public CSVTableModel(CSVReader csvReader) {
		super();
		try {
			data = csvReader.readAll();
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public String getColumnName(int column) {
		String colName = super.getColumnName(column);
		if(isUseFirstRowAsHeader() && data.size() > 0) {
			String[] columnRow = data.get(0);
			if(column < columnRow.length) {
				colName = columnRow[column];
			}
		}
		return colName;
	}
	
	@Override
	public int getRowCount() {
		if(data.size() == 0) return 0;
		if(useFirstRowAsHeader)
			return data.size()-1;
		else
			return data.size();
	}

	@Override
	public int getColumnCount() {
		if(maxCols < 0) {
			for(String[] row:data) {
				maxCols = Math.max(maxCols, row.length);
			}
		}
		return maxCols;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		int rIdx = (useFirstRowAsHeader ? rowIndex+1 : rowIndex);
		final String[] rowData = data.get(rIdx);
		if(columnIndex < rowData.length)
			return rowData[columnIndex];
		else
			return new String();
	}

	public boolean isUseFirstRowAsHeader() {
		return useFirstRowAsHeader;
	}

	public void setUseFirstRowAsHeader(boolean useFirstRowAsHeader) {
		this.useFirstRowAsHeader = useFirstRowAsHeader;
	}

	public String[] deleteRow(int row) {
		final String[] retVal = data.remove(row + 1);
		super.fireTableRowsDeleted(row, row);
		return retVal;
	}

}
