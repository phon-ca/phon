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

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.nodes.query.ColumnOptionsPanel;
import ca.phon.formatter.FormatterUtil;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.util.Tuple;

@OpNodeInfo(
		name="Token Summary",
		description="Calculate unique tokens, number of tokens, and ratio for specified columns",
		category="Table",
		showInLibrary=true
		)
public class TokenSummaryNode extends TableOpNode implements NodeSettings {

	private String columnNames = "";
	private boolean caseSensitive = false;
	private boolean ignoreDiacritics = true;

	private JPanel settingsPanel;
	private ColumnOptionsPanel columnOptions;

	public TokenSummaryNode() {
		super();

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource inputTable = (DefaultTableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();

		final String[] columns = getColumnNames().split(";");
		final Map<String, Tuple<Set<String>, Integer>> columnCounts = new LinkedHashMap<>();
		for(String column:columns) {
			column = column.trim();

			Tuple<Set<String>, Integer> columnCount = new Tuple<>();
			Set<String> tokenSet = new LinkedHashSet<>();
			Integer tokenCount = 0;
			for(int row = 0; row < inputTable.getRowCount(); row++) {
				Object rowVal = inputTable.getValueAt(row, column);
				if(rowVal != null) {
					String rowTxt = FormatterUtil.format(rowVal);
					if(rowTxt.length() > 0) {
						tokenSet.add(rowTxt);
						++tokenCount;
					}
				}
			}
			columnCount.setObj1(tokenSet);
			columnCount.setObj2(tokenCount);
			columnCounts.put(column, columnCount);
		}

		// create table
		final String[] rows = { "Types", "Tokens", "Ratio" };

		for(int row = 0; row < rows.length; row++) {
			List<String> rowData = new ArrayList<>();
			rowData.add(rows[row]);
			for(String columnName:columnCounts.keySet()) {
				final Tuple<Set<String>, Integer> columnCount = columnCounts.get(columnName);

				switch(row) {
				case 0:
					rowData.add(Integer.toString(columnCount.getObj1().size()));
					break;

				case 1:
					rowData.add(Integer.toString(columnCount.getObj2()));
					break;

				case 2:
					float ratio =
						(float)columnCount.getObj1().size() / (float)columnCount.getObj2();
					rowData.add(Float.toString(ratio));
					break;

				default:
					break;
				}
			}
			outputTable.addRow(rowData.toArray());
		}
		int col = 1;
		outputTable.setColumnTitle(0, "Item");
		for(String columnName:columnCounts.keySet()) {
			outputTable.setColumnTitle(col++, columnName);
		}

		context.put(tableOutput, outputTable);
	}

	public String getColumnNames() {
		return (this.columnOptions != null ? this.columnOptions.getColumnNames() : this.columnNames);
	}

	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
		if(this.columnOptions != null)
			this.columnOptions.setColumnNames(columnNames);
	}

	public boolean isCaseSensitive() {
		return (this.columnOptions != null ? this.columnOptions.isCaseSensitive() : this.caseSensitive);
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		if(this.columnOptions != null)
			this.columnOptions.setCaseSensitive(caseSensitive);
	}

	public boolean isIgnoreDiacritics() {
		return (this.columnOptions != null ? this.columnOptions.isIgnoreDiacritics() : this.ignoreDiacritics);
	}

	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
		if(this.columnOptions != null)
			this.columnOptions.setIgnoreDiacritics(ignoreDiacritics);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}

	private JPanel createSettingsPanel() {
		JPanel retVal = new JPanel();
		retVal.setLayout(new BorderLayout());

		columnOptions = new ColumnOptionsPanel();
		columnOptions.setBorder(BorderFactory.createTitledBorder("Columns"));
		columnOptions.setColumnNames(this.columnNames);
		columnOptions.setCaseSensitive(caseSensitive);
		columnOptions.setIgnoreDiacritics(ignoreDiacritics);

		retVal.add(columnOptions, BorderLayout.NORTH);

		return retVal;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();

		retVal.put("columnNames", getColumnNames());
		retVal.put("ignoreDiacritics", isIgnoreDiacritics());
		retVal.put("caseSensitive", isCaseSensitive());

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("columnNames"))
			setColumnNames(properties.getProperty("columnNames"));
		if(properties.containsKey("ignoreDiacritics"))
			setIgnoreDiacritics(Boolean.parseBoolean(properties.getProperty("ignoreDiacritics", "true")));
		if(properties.containsKey("caseSensitive"))
			setCaseSensitive(Boolean.parseBoolean(properties.getProperty("caseSensitive", "false")));
	}



}
