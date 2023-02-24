/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes.table;

import ca.phon.app.opgraph.nodes.table.SortNodeSettings.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.*;
import ca.phon.util.Tuple;

import java.awt.*;
import java.util.List;
import java.util.*;

@OpNodeInfo(
		name="Sort",
		description="Sort table",
		category="Table"
)
public class SortNode extends TableOpNode implements NodeSettings {

	private final static String IPA_COLUMN_PREFIX = "IPA";

	private SortNodeSettingsPanel nodeSettingsPanel;

	public SortNode() {
		super();

		putExtension(SortNodeSettings.class, new SortNodeSettings());
		putExtension(NodeSettings.class, this);
	}

	private void automaticConfiguration(SortNodeSettings settings, OpContext context, DefaultTableDataSource table) {
		settings.clear();
		
		for(int col = 0; col < table.getColumnCount(); col++) {
			String colName = table.getColumnTitle(col);
			Class<?> colType = table.inferColumnType(col);
		
			// stop at number columns
			if(colType.isAssignableFrom(Number.class)) {
				break;
			}
			
			settings.addColumn(colName, (colType == IPATranscript.class || colName.startsWith(IPA_COLUMN_PREFIX) ? SortType.IPA : SortType.PLAIN),
					settings.getAutoSortOrder());
		}
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		
		final SortNodeSettings settings = (SortNodeSettings)getSortSettings().clone();
		if(settings.isConfigureAutomatically()) {
			automaticConfiguration(settings, context, table);
		}

		List<Object[]> rowData = table.getRowData();
		Collections.sort(rowData, new RowComparator(settings, table));

		if(settings.isLikeOnTop() && settings.getSorting().size() > 1) {
			putLikeOnTop(table);
		}

		context.put(tableOutput, table);
	}

	private void putLikeOnTop(DefaultTableDataSource table) {
		Map<String, Tuple<Integer, Integer>> tablePartition = partitionTable(table);

		for(String key:tablePartition.keySet()) {
			Tuple<Integer, Integer> groupInfo = tablePartition.get(key);
			if(groupInfo.getObj2() > 0) {
				Object[] row = table.getRow(groupInfo.getObj2());
				table.deleteRow(groupInfo.getObj2());
				table.insertRow(groupInfo.getObj1(), row);
			}
		}
	}

	/*
	 * Get a map of unique column values along with the first row index
	 * they appear (data should be sorted)
	 *
	 * @return Map of column values (in string form) to a tuple of the startRow
	 * for the group and the row index (if any) of the row with all columns
	 * the same
	 */
	private Map<String, Tuple<Integer, Integer>> partitionTable(DefaultTableDataSource table) {
		Map<String, Tuple<Integer, Integer>> retVal = new LinkedHashMap<>();
		Object currentVal = null;

		int column = table.getColumnIndex(getSortSettings().getSorting().get(0).getColumn());

		for(int row = 0; row < table.getRowCount(); row++) {
			Object val = table.getValueAt(row, column);
			if(val == null) continue;
			if(currentVal == null || cmp(getSortSettings().getSorting().get(0).getType(), currentVal, val) != 0) {
				retVal.put(val.toString(), new Tuple<>(row, -1));
				currentVal = val;
			}

			boolean allLike = true;
			for(int sortCol = 1; sortCol < getSortSettings().getSorting().size() && allLike; sortCol++) {
				int tcol = table.getColumnIndex(getSortSettings().getSorting().get(sortCol).getColumn());
				Object v2 = table.getValueAt(row, tcol);
				if(v2 == null) {
					allLike = false;
					continue;
				}
				allLike &= (cmp(getSortSettings().getSorting().get(sortCol).getType(), val, v2) == 0);
			}

			if(allLike) {
				Tuple<Integer, Integer> rowData = retVal.get(val.toString());
				if(rowData != null)
					rowData.setObj2(row);
			}
		}

		return retVal;
	}

	public SortNodeSettings getSortSettings() {
		return getExtension(SortNodeSettings.class);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(nodeSettingsPanel == null) {
			nodeSettingsPanel = new SortNodeSettingsPanel(getSortSettings());
		}
		return nodeSettingsPanel;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {

	}

	private int cmp(SortType sortType, Object v1, Object v2) throws ProcessingException {
		int retVal = 0;
		// if both values are numbers, sort by number
		if((v1 instanceof Number) && (v2 instanceof Number)) {
			Float f1 = ((Number)v1).floatValue();
			Float f2 = ((Number)v2).floatValue();
			retVal = f1.compareTo(f2);
		} else {
			String v1Txt = (v1 != null ? v1.toString() : "");
			String v2Txt = (v2 != null ? v2.toString() : "");
			if(sortType == SortType.PLAIN) {
				retVal = v1Txt.compareTo(v2Txt);
			} else if(sortType == SortType.IPA) {
				try {
					// HACK remove all '.' now so segmental relations results pass through
					v1Txt = v1Txt.replaceAll("\\.", "");
					v2Txt = v2Txt.replaceAll("\\.", "");
					IPATranscript v1ipa =
							(v1 != null && v1 instanceof IPATranscript ? (IPATranscript)v1 : IPATranscript.parseIPATranscript(v1Txt));
					IPATranscript v2ipa =
							(v2 != null && v2 instanceof IPATranscript ? (IPATranscript)v2 : IPATranscript.parseIPATranscript(v2Txt));

					retVal = v1ipa.compareTo(v2ipa);
				} catch (java.text.ParseException pe) {
					throw new ProcessingException(null, pe);
				}

			}
		}
		return retVal;
	}

	private class RowComparator implements Comparator<Object[]> {

		private TableDataSource table;

		private SortNodeSettings settings;
		
		public RowComparator(SortNodeSettings settings, TableDataSource table) {
			this.settings = settings;
			this.table = table;
		}

		@Override
		public int compare(Object[] row1, Object[] row2) {
			int retVal = 0;

			for (int i = 0; i < settings.getSorting().size(); i++) {
				SortColumn sc = settings.getSorting().get(i);
				final int colIdx = getColumnIndex(table, sc.getColumn());
				if (colIdx < 0) continue;
				final Object v1 = row1[colIdx];
				final Object v2 = row2[colIdx];

				retVal = cmp(sc.getType(), v1, v2);

				// reverse if necessary
				if (sc.getOrder() == SortOrder.DESCENDING) {
					retVal *= -1;
				}

				// only continue if necessary
				if (retVal != 0) break;
			}

			return retVal;
		}
	}

}
