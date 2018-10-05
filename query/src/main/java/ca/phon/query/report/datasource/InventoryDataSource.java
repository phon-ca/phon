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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.phon.ipa.IPATranscript;
import ca.phon.query.db.ReportHelper;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.io.InventorySection;
import ca.phon.util.CollatorFactory;
import ca.phon.util.Tuple;

/**
 * Inventory data source for (now deprecated) reports.
 *
 * @deprecated
 */
@Deprecated
public class InventoryDataSource implements TableDataSource {

	/**
	 * Inventory format info
	 */
	private InventorySection invData;

	/**
	 * List of searches
	 */
	private ResultSet[] searches;

	/**
	 * Map of inventories based on result format.
	 * By default (i.e., not grouped by format) all
	 * results go int the inventory for key 'ALL'
	 */
	private Map<String, Map<String, Integer[]>> inventories =
		new HashMap<String, Map<String, Integer[]>>();
	private Map<String, ArrayList<String>> orderedKeys =
		new HashMap<String, ArrayList<String>>();

	/**
	 * Include excluded results in report?
	 */
	private boolean includeExcluded;

	/**
	 * Constructor
	 */
	public InventoryDataSource(ResultSet[] searches, InventorySection data) {
		this.invData = data;
		this.searches = searches;
		this.includeExcluded = data.isIncludeExcluded();

		TreeMap<String, Integer[]> defaultCounter =
			new TreeMap<String, Integer[]>();
		inventories.put("ALL", defaultCounter);

		generateInventory();
	}

	@Override
	public int getColumnCount() {
		int numCols =
			1 + // result string
			searches.length + // col for each session
			(invData.isGroupByFormat() ? 1 : 0); // result format when changing
		return numCols;
	}

	// cached row count
	int numRows = -1;
	@Override
	public int getRowCount() {
		if(numRows < 0) {
			numRows = 0;
			for(Map<String, Integer[]> counter:inventories.values()) {
				numRows += counter.size();
			}
		}
		return numRows;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = "";

		if(invData.isGroupByFormat()) {
			List<String> formatKeys = new ArrayList<String>();
			formatKeys.addAll(inventories.keySet());
			formatKeys.remove("ALL");
			Collections.sort(formatKeys, CollatorFactory.defaultCollator());

			int cIdx = 0;
			for(int i = 0; i < formatKeys.size(); i++) {
				String format = formatKeys.get(i);

				Map<String, Integer[]> counter = inventories.get(format);
				if(row < cIdx + counter.size()) {
					final List<String> resultKeys = orderedKeys.get(format);

					int subIdx = row - cIdx;

					String rVal = resultKeys.get(subIdx);
					if(col == 0)
						retVal = rVal;
					else if (col < getColumnCount()-1)
						retVal = counter.get(rVal)[col-1];
					else
						retVal = (subIdx == 0 ? format : "");

					break;
				} else {
					cIdx += counter.size();
				}
			}
		} else {
			Map<String, Integer[]> counter = inventories.get("ALL");
			final List<String> keys = orderedKeys.get("ALL");

			String key = keys.get(row);
			Integer[] count = counter.get(key);

			if (col == 0) retVal = key;
			else retVal = count[col-1];
		}

		return retVal;
	}

	@Override
	public String getColumnTitle(int col) {
		String retVal = "";

		if(col == 0) {
			retVal = "";
		} else {
			int searchIdx = col - 1;
			if(searchIdx < searches.length)
				retVal = searches[searchIdx].getSessionPath();
			else
				retVal = "Result Format";
		}

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

	/*
	 * Generate inventory
	 */
	private void generateInventory() {
		int len = searches.length;

		int sIdx = 0;
		for(ResultSet s:searches) {
			final int numResults = s.size();
			for(int rIdx = 0; rIdx < numResults; rIdx++) {
				Result result = s.getResult(rIdx);

				if(result.isExcluded()) {
					if(!isIncludeExcluded()) continue;
				}

//				String resultStr = ReportHelper.createResultString(result);
				Tuple<String, String> data = createResultData(result);

				Map<String, Integer[]> counter = null;
				if(invData.isGroupByFormat()) {
					counter = inventories.get(data.getObj2());
					if(counter == null) {
						counter = new TreeMap<String, Integer[]>();
						inventories.put(data.getObj2(), counter);
					}
				} else {
					counter = inventories.get("ALL");
				}

				Integer[] vals = counter.get(data.getObj1());
				if(vals == null) {
					vals = new Integer[len];
					for(int i = 0; i <  len; i++) vals[i] = 0;
					counter.put(data.getObj1(), vals);
				}
				vals[sIdx]++;
			}
			sIdx++;
		}

		for(String key:inventories.keySet()) {
			final ArrayList<String> ordered = new ArrayList<String>(inventories.get(key).keySet());
			Collections.sort(ordered, CollatorFactory.defaultCollator());
			orderedKeys.put(key, ordered);
		}
	}

	/*
	 * String helper methods
	 */
	private String stripDiacritics(String txt) {
		String retVal = txt.trim();
		try {
			final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
			retVal = ipa.removePunctuation().stripDiacritics().toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	/*
	 * Returns a tuple of result value, result format
	 *
	 */
	private Tuple<String, String> createResultData(Result result) {
		String resVal = ReportHelper.createResultString(result);
		String resFormat = ReportHelper.createResultSchemaString(result);

		String retVal = "";
		String retFormat = "";

		// data to include
		if(invData.isIncludeResultValue()) {
			retVal = resVal;
			retFormat = resFormat;
		}

		if(invData.isIncludeMetadata()) {
			Map<String, String> metadata = result.getMetadata();

			String metaFormat = "(";
			String metaVal = "(";

			for(String metakey:metadata.keySet()) {
				String metaval = metadata.get(metakey);
				if(metaval != null && metaval.length() > 0) {
					metaFormat += metakey + ";";
					metaVal += metaval + ";";
				}
			}

			metaFormat += ")";
			metaVal += ")";

			retVal += (retVal.length() > 0 ? " " : "") + metaVal;
			retFormat += (retFormat.length  () > 0 ? " " : "") + metaFormat;
		}

		// diacritics/case sensitivity
		if(!invData.isCaseSensitive()) {
			retVal = retVal.toLowerCase();
			retFormat = retFormat.toLowerCase();
		}

		if(invData.isIgnoreDiacritics()) {
			retVal = stripDiacritics(retVal);
		}

		return new Tuple<String, String>(retVal, retFormat);
	}

	public boolean isIncludeExcluded() {
		return includeExcluded;
	}

	public void setIncludeExcluded(boolean includeExcluded) {
		this.includeExcluded = includeExcluded;
	}

}
