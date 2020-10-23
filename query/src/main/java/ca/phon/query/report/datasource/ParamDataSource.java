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

import java.util.*;

import ca.phon.query.db.*;
import ca.phon.query.report.io.*;
import ca.phon.util.*;

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
