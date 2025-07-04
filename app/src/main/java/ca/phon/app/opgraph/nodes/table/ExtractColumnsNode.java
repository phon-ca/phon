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

import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.*;
import ca.phon.ui.text.PromptedTextField;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

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
