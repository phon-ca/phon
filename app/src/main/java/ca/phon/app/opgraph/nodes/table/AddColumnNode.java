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
package ca.phon.app.opgraph.nodes.table;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import org.mozilla.javascript.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.query.report.datasource.*;
import ca.phon.script.*;
import ca.phon.ui.text.*;
import ca.phon.util.resources.*;

/**
 * Add a new column to the given table using
 * a user-entered script.
 *
 */
@OpNodeInfo(
		category="Table",
		description="Add a new column to the given table using a script.",
		name="Add Column",
		showInLibrary=true
)
public class AddColumnNode extends TableScriptNode {

	private final static String ADD_COLUMN_SCRIPT_LIST = "ca/phon/app/opgraph/nodes/table/addcolumn/addcolumn_scripts";

	private final static String DEFAULT_SCRIPT = "/*\n" +
			"params = {label, \"Add a new column to the input table using javascript.\", \"<html><b>Add column to table</b></html>\"}\n" +
			";\n" +
			"*/\n" +
			"\n" +
			"function getRowValue(table, row) {\n" +
			"	return new String();\n" +
			"}\n" +
			"";

	public static ResourceLoader<URL> getAddColumnScriptResourceLoader() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();

		// add classpath handler
		final ClassLoaderHandler<URL> handler = new ClassLoaderHandler<URL>() {

			@Override
			public URL loadFromURL(URL url) throws IOException {
				return url;
			}

		};
		handler.loadResourceFile(ADD_COLUMN_SCRIPT_LIST);
		retVal.addHandler(handler);

		return retVal;
	}

	// settings
	private String columnName = "NewColumn";

	private int columnIndex = -1;

	private PromptedTextField columnIndexField;

	// UI
	private PromptedTextField columnNameField;

	public AddColumnNode() {
		super(DEFAULT_SCRIPT);
	}

	public AddColumnNode(PhonScript script) {
		super(script);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();

		final TableDataSource table = super.getInputTable(context);

		final DefaultTableDataSource outputTable = new DefaultTableDataSource();

		int columnCount = table.getColumnCount() + 1;
		int colIndex = (getColumnIndex() >= 0 ? getColumnIndex() : columnCount-1);

		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);

			for(int row = 0; row < table.getRowCount(); row++) {
				checkCanceled();

				final Object[] rowData = new Object[columnCount];


				for(int col = 0; col < colIndex; col++) {
					rowData[col] = table.getValueAt(row, col);
				}
				for(int col = colIndex+1; (col-1) < table.getColumnCount(); col++) {
					rowData[col] = table.getValueAt(row, col-1);
				}

				Object newVal = scriptContext.callFunction(scope, "getRowValue",
						table, row);
				if(newVal instanceof NativeJavaObject) {
					newVal = ((NativeJavaObject)newVal).unwrap();
				}

				rowData[colIndex] = (newVal != null ? newVal : new String());

				// add row to outputtable
				outputTable.addRow(rowData);
			}
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e);
		}

		for(int col = 0; col < colIndex; col++)
			outputTable.setColumnTitle(col, table.getColumnTitle(col));
		outputTable.setColumnTitle(colIndex, getColumnName());
		for(int col = colIndex+1; (col-1) < table.getColumnCount(); col++)
			outputTable.setColumnTitle(col, table.getColumnTitle(col-1));

		context.put(tableOutput, outputTable);
	}

	public ResourceLoader<URL> getAddColumnScriptLibrary() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();



		return retVal;
	}

	@Override
	protected JComponent createSettingsPanel() {
		final JComponent scriptPanel = super.createSettingsPanel();

		final GridBagLayout layout = new GridBagLayout();
		final JPanel settingsPanel = new JPanel(layout);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;

		settingsPanel.add(new JLabel("Column Name:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		columnNameField = new PromptedTextField("Enter new column name");
		columnNameField.setText(this.columnName);
		settingsPanel.add(columnNameField, gbc);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.weightx = 0.0;
		settingsPanel.add(new JLabel("Column Index:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		columnIndexField = new PromptedTextField("Enter column index (optional)");
		if(columnIndex >= 0)
			columnIndexField.setText(Integer.toString(columnIndex));
		settingsPanel.add(columnIndexField, gbc);
		
		JPanel retVal = new JPanel(new BorderLayout());

		retVal.add(settingsPanel, BorderLayout.NORTH);
		retVal.add(scriptPanel, BorderLayout.CENTER);

		return retVal;
	}

	public String getColumnName() {
		return (columnNameField != null ? columnNameField.getText() : columnName);
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getColumnIndex() {
		return (columnIndexField != null && columnIndexField.getText().trim().length() > 0 ? Integer.parseInt(columnIndexField.getText()) : columnIndex);
	}

	public void setColumnIndex(int colIndex) {
		this.columnIndex = colIndex;
		if(columnIndexField != null) {
			columnIndexField.setText(Integer.toString(colIndex));
		}
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = super.getSettings();

		retVal.setProperty("columnIndex", Integer.toString(getColumnIndex()));
		retVal.setProperty("column", getColumnName());

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		super.loadSettings(properties);

		setColumnName(properties.getProperty("column", "MyColumn"));
		setColumnIndex(Integer.parseInt(properties.getProperty("columnIndex", "-1")));
	}

}
