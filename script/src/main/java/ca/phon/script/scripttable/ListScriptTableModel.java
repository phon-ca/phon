package ca.phon.script.scripttable;

import java.util.List;
import java.util.Map;

/**
 * A table model that uses a list of object as row data.  The following
 * properties are supplied to each column script:
 * 
 *  * row - integer for the current row
 *  * col - integer for the current column
 *  * rowValue - value of data for row
 * 
 */
public class ListScriptTableModel<T> extends AbstractScriptTableModel {

	private static final long serialVersionUID = 3277560475293409146L;

	private final List<T> rowData;
	
	public ListScriptTableModel(List<T> rowData) {
		super();
		this.rowData = rowData;
	}
	
	@Override
	public int getRowCount() {
		return rowData.size();
	}

	@Override
	public Map<String, Object> getMappingsAt(int row, int col) {
		final Map<String, Object> retVal = super.getMappingsAt(row, col);
		
		retVal.put("row", row);
		retVal.put("col", col);
		retVal.put("rowData", rowData.get(row));
		
		return retVal;
	}
	
	/**
	 * Return row data
	 * 
	 * @return row data
	 */
	public List<T> getRowData() {
		return this.rowData;
	}
}
