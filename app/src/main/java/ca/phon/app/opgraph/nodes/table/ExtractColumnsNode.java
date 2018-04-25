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

import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.*;
import ca.phon.ui.text.PromptedTextField;

@OpNodeInfo(
		name="Extract Columns",
		description="Extract columns from table",
		category="Table"
)
public class ExtractColumnsNode extends TableOpNode implements NodeSettings {

	private String columns;

	private JPanel settingsPanel;

	private JTextField columnsField;

	public ExtractColumnsNode() {
		super();

		putExtension(NodeSettings.class, this);
	}

	public List<String> getColumns() {
		return
			Arrays.asList((columnsField != null ? columnsField.getText() : columns).split(";"));
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource table = (TableDataSource)context.get(tableInput);

		final int[] columnIndices = getColumnIndices(table, getColumns());
		final DefaultTableDataSource outputTable =
				new DefaultTableDataSource();

		Object[] rowData = new Object[columnIndices.length];
		for(int i = 0; i < table.getRowCount(); i++) {
			checkCanceled();
			for(int j = 0; j < rowData.length; j++) {
				final int colIdx = columnIndices[j];
				rowData[j] =
						(colIdx >= 0 && colIdx < table.getColumnCount() ? table.getValueAt(i, colIdx) : null);
			}
			outputTable.addRow(rowData);
		}

		for(int i = 0; i < columnIndices.length; i++) {
			final int colIdx = columnIndices[i];
			outputTable.setColumnTitle(i, table.getColumnTitle(colIdx));
		}

		context.put(tableOutput, outputTable);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new GridBagLayout());
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(2, 2, 5, 2);

			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			settingsPanel.add(new JLabel("Columns:"), gbc);

			gbc.gridx++;
			gbc.weightx = 1.0;
			columnsField = new PromptedTextField("Enter column names/numbers separated by ';'");
			if(this.columns != null)
				columnsField.setText(this.columns);
			settingsPanel.add(columnsField, gbc);

		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		if(this.columns != null)
			props.put("columns", this.columns);
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("columns")) {
			columns = properties.getProperty("columns", "");
		}
	}

}
