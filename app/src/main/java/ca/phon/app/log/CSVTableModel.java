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
package ca.phon.app.log;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;

import javax.swing.table.AbstractTableModel;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Table model using a CSV reader for table data.
 */
public class CSVTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 5228655036751277639L;

	private static final Logger LOGGER = Logger
			.getLogger(CSVTableModel.class.getName());

	private boolean useFirstRowAsHeader = true;
	
	private int maxCols = -1;
	
	private List<String[]> data = new ArrayList<String[]>();
	
	public CSVTableModel(CSVReader csvReader) {
		super();
		try {
			data = csvReader.readAll();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
