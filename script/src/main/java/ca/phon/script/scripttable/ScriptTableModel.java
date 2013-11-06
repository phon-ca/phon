package ca.phon.script.scripttable;

import java.util.Map;

import javax.swing.table.TableModel;

import ca.phon.script.PhonScript;

/**
 * Model for a scripted table.  The values for each
 * column are controlled by a script.
 * 
 * 
 */
public interface ScriptTableModel extends TableModel {
	
	/**
	 * Return the number of rows
	 * 
	 * @return number of row in the table
	 */
	public int getRowCount();
	
	/**
	 * Return the number of columns
	 * 
	 * @return number of columns
	 */
	public int getColumnCount();
	
	/**
	 * Get the class for the specified column
	 * 
	 * @param col the column index
	 * @return the class for the specified column
	 */
	public Class<?> getColumnClass(int col);
	
	/**
	 * Get the name of the specified column
	 * 
	 * @param col the column index
	 * @return the title of the specified column.
	 */
	public String getColumnName(int col);
	
	/**
	 * Get the script for the given column.
	 * 
	 * @param col
	 * 
	 * @return script for the specified column index
	 */
	public PhonScript getColumnScript(int col);
	
//	/**
//	 * Get the script mimetype for the given colum.
//	 * 
//	 * @param col
//	 * @return mimetype for script, 'text/javascript' by
//	 *  default.
//	 */
//	public String getColumnScriptMimetype(int col);
	
	/**
	 * Get value at given row,col
	 * 
	 * @param row
	 * @param col
	 * @return value for the specified cell
	 */
	public Object getValueAt(int row, int col);
	
	/**
	 * Variable mapping for the given cell.
	 * 
	 * @param row
	 * @param col
	 * 
	 * @return the column mapping for the specified cell
	 */
	public Map<String, Object> getMappingsAt(int row, int col);
	
}
