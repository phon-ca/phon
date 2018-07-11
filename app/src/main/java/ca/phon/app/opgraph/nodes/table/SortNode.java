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
package ca.phon.app.opgraph.nodes.table;

import java.awt.Component;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import ca.phon.app.opgraph.nodes.table.SortNodeSettings.SortColumn;
import ca.phon.app.opgraph.nodes.table.SortNodeSettings.SortOrder;
import ca.phon.app.opgraph.nodes.table.SortNodeSettings.SortType;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Sort",
		description="Sort table",
		category="Table"
)
public class SortNode extends TableOpNode implements NodeSettings {

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
			
			settings.addColumn(colName, (colType == IPATranscript.class ? SortType.IPA : SortType.PLAIN), 
					settings.getAutoSortOrder());
		}
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		
		final SortNodeSettings settings = (SortNodeSettings)getSortSettings().clone();
		if(settings.isConfigureAutomatically()) {
			automaticConfiguration(settings, context, table);
		} else {
			System.out.println("Here");
		}

		List<Object[]> rowData = table.getRowData();
		Collections.sort(rowData, new RowComparator(settings, table));

		context.put(tableOutput, table);
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

			for(SortColumn sc:settings.getSorting()) {
				final int colIdx = getColumnIndex(table, sc.getColumn());
				if(colIdx < 0) continue;
				final Object v1 = row1[colIdx];
				final Object v2 = row2[colIdx];

				// if both values are numbers, sort by number
				if((v1 instanceof Number) && (v2 instanceof Number)) {
					Float f1 = ((Number)v1).floatValue();
					Float f2 = ((Number)v2).floatValue();
					retVal = f1.compareTo(f2);
				} else {
					String v1Txt = (v1 != null ? v1.toString() : "");
					String v2Txt = (v2 != null ? v2.toString() : "");
					if(sc.getType() == SortType.PLAIN) {
						retVal = v1Txt.compareTo(v2Txt);
					} else if(sc.getType() == SortType.IPA) {
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

				// reverse if necessary
				if(sc.getOrder() == SortOrder.DESCENDING) {
					retVal *= -1;
				}

				// only continue if necessary
				if(retVal != 0) break;
			}

			return retVal;
		}

	}

}
