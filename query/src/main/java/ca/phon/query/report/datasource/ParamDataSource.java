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

import java.util.*;

import ca.phon.query.db.*;
import ca.phon.query.report.io.ParamSection;
import ca.phon.util.CollatorFactory;

/**
 * List query parameters.
 *
 * @deprecated
 */
@Deprecated
public class ParamDataSource implements TableDataSource {

	/**
	 * The query
	 */
	private Query q;

	/**
	 * ParamsSection info
	 */
	private ParamSection invData;

	public ParamDataSource(Query q, ParamSection info) {
		this.q = q;
		this.invData = info;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return q.getScript().getParameters().size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = new String();

		final Script qScript = q.getScript();
		Collection<String> keys = qScript.getParameters().keySet();
		List<String> keyList = new ArrayList<String>(keys);
		Collections.sort(keyList, CollatorFactory.defaultCollator());

		if(col == 0)
			retVal = keyList.get(row);
		else if (col == 1)
			retVal = qScript.getParameters().get(keyList.get(row));

		return retVal;
	}

	@Override
	public String getColumnTitle(int col) {
		String retVal = "";

		if(col == 0)
			retVal = "Param Name";
		else if (col == 1)
			retVal = "Value";

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
