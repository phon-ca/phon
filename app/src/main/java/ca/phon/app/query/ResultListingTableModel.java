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
package ca.phon.app.query;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import ca.phon.query.db.ResultSet;
import ca.phon.query.report.datasource.ResultListingDataSource;
import ca.phon.query.report.io.ResultListing;
import ca.phon.session.Session;
import ca.phon.util.Tuple;

/**
 * Displays the contents of a resultset using
 * a {@link ResultListingDataSource} backend.
 * 
 */
public class ResultListingTableModel extends ResultListingDataSource {
	
	private final Map<CellLocation, Object> cache =
			Collections.synchronizedMap(new TreeMap<CellLocation, Object>());
	
	public ResultListingTableModel(Session session, ResultSet rs, ResultListing listing) {
		super(session, rs, listing);
		setIncludeExcluded(true);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final CellLocation loc = new CellLocation();
		loc.setObj1(rowIndex);
		loc.setObj2(columnIndex);
		
		Object retVal = cache.get(loc);
		if(retVal == null) {
			retVal = super.getValueAt(rowIndex, columnIndex);
			cache.put(loc, retVal);
		}
		return retVal;
	}

	@Override
	public void fireTableStructureChanged() {
		cache.clear();
		super.fireTableStructureChanged();
	}
	
	@Override
	public void fireTableDataChanged() {
		cache.clear();
		super.fireTableDataChanged();
	}
	
	@Override
	public void setResultSet(ResultSet rs) {
		super.setResultSet(rs);
		fireTableDataChanged();
	}
	
	@Override
	public void setListing(ResultListing listing) {
		super.setListing(listing);
		fireTableStructureChanged();
	}
	
	/**
	 * Location information
	 */
	private class CellLocation extends Tuple<Integer, Integer> {
		
		public int getRow() {
			return (super.getObj1() == null ? -1 : super.getObj1());
		}
		
		public int getColumn() {
			return (super.getObj2() == null ? -1 : super.getObj2());
		}
		
	}

}
