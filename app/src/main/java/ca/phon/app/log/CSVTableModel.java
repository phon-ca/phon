package ca.phon.app.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		if(useFirstRowAsHeader && data.size() > 0) {
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

}
