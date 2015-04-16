/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
