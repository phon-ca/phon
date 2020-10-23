/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.query;

import java.util.*;

import ca.phon.query.db.*;
import ca.phon.query.report.datasource.*;
import ca.phon.query.report.io.*;
import ca.phon.session.*;
import ca.phon.util.*;

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
