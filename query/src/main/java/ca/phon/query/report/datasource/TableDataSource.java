/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.query.report.datasource;

/**
 * An interface for implementing a report data source
 * in a tabular format.
 *
 *
 */
public interface TableDataSource {

	/**
	 * Number of columns (not including column header)
	 */
	public int getColumnCount();

	/**
	 * Number of rows (not including column header)
	 */
	public int getRowCount();

	/**
	 * Return the value at the given row+col.
	 *
	 * @param row
	 * @param col
	 * @return the value at the given pos
	 */
	public Object getValueAt(int row, int col);

	/**
	 * Get the name of the specified column
	 *
	 * @param col
	 * @return the column title
	 */
	public String getColumnTitle(int col);

	public int getColumnIndex(String colname);

}
