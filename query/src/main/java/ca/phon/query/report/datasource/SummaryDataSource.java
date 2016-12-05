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

import ca.phon.query.db.ResultSet;
import ca.phon.query.report.io.SummarySection;

/**
 * Displays a list of session  names along with
 * the number of results found in each session.
 * @deprecated
 */
@Deprecated
public class SummaryDataSource implements TableDataSource {

	private ResultSet[] resultSets;
	
	/**
	 * Section info
	 */
	private SummarySection invData;
	
	public SummaryDataSource(ResultSet[] resultSets, SummarySection info) {
		this.resultSets = resultSets;
		this.invData = info;
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return resultSets.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
//		Long sid = invData.getSid().get(row);
		ResultSet s = resultSets[row];
		
		Object retVal = "";
		
		int allResults = s.numberOfResults(true);
		int allNonExluded = s.numberOfResults(false);
		int numExcluded = allResults - allNonExluded;
		
		if(col == 0)
			retVal = s.getSessionPath();
		else if (col == 1)
			retVal = allResults - numExcluded;
		else if (col == 2)
			retVal = numExcluded;
		
		return retVal;
	}

	@Override
	public String getColumnTitle(int col) {
		String retVal = "";
		
		if(col == 0)
			retVal = "Session Name";
		else if(col == 1)
			retVal = "# Results";
		else if(col == 3)
			retVal = "# Results Excluded";
		
		return retVal;
	}

}
