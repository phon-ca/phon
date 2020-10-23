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
package ca.phon.query.report.datasource;

import ca.phon.query.db.*;
import ca.phon.query.report.io.*;

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

	@Override
	public int getColumnIndex(String columnName) {
		int colIdx = -1;
		for(int c = 0; c < getColumnCount(); c++) {
			if(getColumnTitle(c).equals(columnName)) {
				colIdx = c;
				break;
			}
		}
		return colIdx;
	}

}
