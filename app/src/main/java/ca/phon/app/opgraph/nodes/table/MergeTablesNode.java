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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTitledSeparator;

import ca.phon.app.opgraph.nodes.query.ColumnOptionsPanel;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.TableUtils;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.ui.text.PromptedTextField;

@OpNodeInfo(
		name="Merge Tables",
		description="Merge row data from two tables using given columns as primary keys.",
		category="Table",
		showInLibrary=true
)
public class MergeTablesNode extends TableOpNode implements NodeSettings {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MergeTablesNode.class.getName());

	private InputField table1Input = new InputField("table1", "Table 1", false, true, TableDataSource.class);

	private InputField table2Input = new InputField("table2", "Table 2", false, true, TableDataSource.class);

	private boolean interleaveColumns = true;

	private int table1ColumnRatio = 1;

	private int table2ColumnRatio = 1;

	private String table1KeyColumn = "";
	private boolean table1KeyColumnCaseSensitive = false;
	private boolean table1KeyColumnIgnoreDiacritics = true;

	private String table2KeyColumn = "";
	private boolean table2KeyColumnCaseSensitive = false;
	private boolean table2KeyColumnIgnoreDiacritics = true;

	private String keyColumnName = "";

	private String table1ColumnPrefix = "";
	private String table1ColumnSuffix = "";

	private String table2ColumnPrefix = "";
	private String table2ColumnSuffix = "";

	private JPanel settingsPanel;

	private PromptedTextField keyColumnNameField;

	private ColumnOptionsPanel table1KeyColumnPanel;
	private ColumnOptionsPanel table2KeyColumnPanel;

	private PromptedTextField table1ColumnPrefixField;
	private PromptedTextField table1ColumnSuffixField;

	private PromptedTextField table2ColumnPrefixField;
	private PromptedTextField table2ColumnSuffixField;

	private JCheckBox interleaveColumnsBox;

	private PromptedTextField table1ColumnRatioField;
	private PromptedTextField table2ColumnRatioField;

	public MergeTablesNode() {
		super();

		removeField(tableInput);

		putField(table1Input);
		putField(table2Input);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}

	private JPanel createSettingsPanel() {
		final JPanel retVal = new JPanel();

		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		retVal.setLayout(layout);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(2, 2, 5, 2);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		retVal.add(new JXTitledSeparator("Table 1 Key Column"), gbc);

		++gbc.gridy;
		table1KeyColumnPanel = new ColumnOptionsPanel();
		table1KeyColumnPanel.setColumnNames(this.table1KeyColumn);
		table1KeyColumnPanel.setCaseSensitive(this.table1KeyColumnCaseSensitive);
		table1KeyColumnPanel.setIgnoreDiacritics(this.table1KeyColumnIgnoreDiacritics);
		retVal.add(table1KeyColumnPanel, gbc);

		++gbc.gridy;
		retVal.add(new JXTitledSeparator("Table 2 Key Column"), gbc);

		++gbc.gridy;
		table2KeyColumnPanel = new ColumnOptionsPanel();
		table2KeyColumnPanel.setColumnNames(this.table2KeyColumn);
		table2KeyColumnPanel.setCaseSensitive(this.table2KeyColumnCaseSensitive);
		table2KeyColumnPanel.setIgnoreDiacritics(this.table2KeyColumnIgnoreDiacritics);
		retVal.add(table2KeyColumnPanel, gbc);

		++gbc.gridy;
		retVal.add(new JXTitledSeparator("Column Naming"), gbc);

		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		retVal.add(new JLabel("Key column name:"), gbc);
		++gbc.gridx;
		keyColumnNameField = new PromptedTextField("Enter key column name");
		keyColumnNameField.setText(keyColumnName);
		gbc.weightx = 1.0;
		retVal.add(keyColumnNameField, gbc);

		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		retVal.add(new JLabel("Table 1 column prefix:"), gbc);
		++gbc.gridx;
		table1ColumnPrefixField = new PromptedTextField("Enter column prefix");
		table1ColumnPrefixField.setText(this.table1ColumnPrefix);
		gbc.weightx = 1.0;
		retVal.add(table1ColumnPrefixField, gbc);

		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		retVal.add(new JLabel("Table 1 column suffix:"), gbc);
		++gbc.gridx;
		table1ColumnSuffixField = new PromptedTextField("Enter column suffix");
		table1ColumnSuffixField.setText(this.table1ColumnSuffix);
		gbc.weightx = 1.0;
		retVal.add(table1ColumnSuffixField, gbc);

		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		retVal.add(new JLabel("Table 2 column prefix:"), gbc);
		++gbc.gridx;
		table2ColumnPrefixField = new PromptedTextField("Enter column prefix");
		table2ColumnPrefixField.setText(this.table2ColumnPrefix);
		gbc.weightx = 1.0;
		retVal.add(table2ColumnPrefixField, gbc);

		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		retVal.add(new JLabel("Table 2 column suffix:"), gbc);
		++gbc.gridx;
		table2ColumnSuffixField = new PromptedTextField("Enter column suffix");
		table2ColumnSuffixField.setText(this.table2ColumnSuffix);
		gbc.weightx = 1.0;
		retVal.add(table2ColumnSuffixField, gbc);

		++gbc.gridy;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		retVal.add(new JXTitledSeparator("Column interleaving"), gbc);

		++gbc.gridy;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		interleaveColumnsBox = new JCheckBox("Interleave columns");
		interleaveColumnsBox.setSelected(this.interleaveColumns);
		retVal.add(interleaveColumnsBox, gbc);

		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		retVal.add(new JLabel("Table 1 column ratio:"), gbc);

		++gbc.gridx;
		gbc.weightx = 1.0;
		table1ColumnRatioField = new PromptedTextField("Enter column ratio");
		table1ColumnRatioField.setText(Integer.toString(table1ColumnRatio));
		retVal.add(table1ColumnRatioField, gbc);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		retVal.add(new JLabel("Table 2 column ratio:"), gbc);

		++gbc.gridx;
		gbc.weightx = 1.0;
		table2ColumnRatioField = new PromptedTextField("Enter column ratio");
		table2ColumnRatioField.setText(Integer.toString(table2ColumnRatio));
		retVal.add(table2ColumnRatioField, gbc);

		return retVal;
	}

	public String getTable1KeyColumn() {
		return (table1KeyColumnPanel != null ? table1KeyColumnPanel.getColumnNames() : this.table1KeyColumn);
	}

	public void setTable1KeyColumn(String table1KeyColumn) {
		this.table1KeyColumn = table1KeyColumn;
		if(table1KeyColumnPanel != null)
			table1KeyColumnPanel.setColumnNames(table1KeyColumn);
	}

	public boolean getTable1KeyColumnCaseSensitive() {
		return (table1KeyColumnPanel != null ? table1KeyColumnPanel.isCaseSensitive() : this.table1KeyColumnCaseSensitive);
	}

	public void setTable1KeyColumnCaseSensitive(boolean table1KeyColumnCaseSensitive) {
		this.table1KeyColumnCaseSensitive = table1KeyColumnCaseSensitive;
		if(this.table1KeyColumnPanel != null)
			this.table1KeyColumnPanel.setCaseSensitive(table1KeyColumnCaseSensitive);
	}

	public boolean getTable1KeyColumnIgnoreDiacritics() {
		return (table1KeyColumnPanel != null ? table1KeyColumnPanel.isIgnoreDiacritics() : this.table1KeyColumnIgnoreDiacritics);
	}

	public void setTable1KeyColumnIgnoreDiacritics(boolean table1KeyColumnIgnoreDiacritics) {
		this.table1KeyColumnIgnoreDiacritics = table1KeyColumnIgnoreDiacritics;
		if(this.table1KeyColumnPanel != null)
			this.table1KeyColumnPanel.setIgnoreDiacritics(table1KeyColumnIgnoreDiacritics);
	}

	public String getKeyColumnName() {
		return (keyColumnNameField != null ? keyColumnNameField.getText() : keyColumnName);
	}

	public void setKeyColumnName(String keyColumnName) {
		this.keyColumnName = keyColumnName;
		if(this.keyColumnNameField != null)
			this.keyColumnNameField.setText(keyColumnName);
	}

	public String getTable1ColumnPrefix() {
		return (table1ColumnPrefixField != null ? table1ColumnPrefixField.getText() : table1ColumnPrefix);
	}

	public void setTable1ColumnPrefix(String table1ColumnPrefix) {
		this.table1ColumnPrefix = table1ColumnPrefix;
		if(table1ColumnPrefixField != null)
			table1ColumnPrefixField.setText(table1ColumnPrefix);
	}

	public String getTable1ColumnSuffix() {
		return (table1ColumnSuffixField != null ? table1ColumnSuffixField.getText() : table1ColumnSuffix);
	}

	public void setTable1ColumnSuffix(String table1ColumnSuffix) {
		this.table1ColumnSuffix = table1ColumnSuffix;
		if(table1ColumnSuffixField != null)
			table1ColumnSuffixField.setText(table1ColumnSuffix);
	}

	public String getTable2KeyColumn() {
		return (table2KeyColumnPanel != null ? table2KeyColumnPanel.getColumnNames() : this.table2KeyColumn);
	}

	public void setTable2KeyColumn(String table2KeyColumn) {
		this.table2KeyColumn = table2KeyColumn;
		if(table2KeyColumnPanel != null)
			table2KeyColumnPanel.setColumnNames(table2KeyColumn);
	}

	public boolean getTable2KeyColumnCaseSensitive() {
		return (table2KeyColumnPanel != null ? table2KeyColumnPanel.isCaseSensitive() : this.table2KeyColumnCaseSensitive);
	}

	public void setTable2KeyColumnCaseSensitive(boolean table2KeyColumnCaseSensitive) {
		this.table2KeyColumnCaseSensitive = table2KeyColumnCaseSensitive;
		if(this.table2KeyColumnPanel != null)
			this.table2KeyColumnPanel.setCaseSensitive(table2KeyColumnCaseSensitive);
	}

	public boolean getTable2KeyColumnIgnoreDiacritics() {
		return (table2KeyColumnPanel != null ? table2KeyColumnPanel.isIgnoreDiacritics() : this.table2KeyColumnIgnoreDiacritics);
	}

	public void setTable2KeyColumnIgnoreDiacritics(boolean table2KeyColumnIgnoreDiacritics) {
		this.table2KeyColumnIgnoreDiacritics = table2KeyColumnIgnoreDiacritics;
		if(this.table2KeyColumnPanel != null)
			this.table2KeyColumnPanel.setIgnoreDiacritics(table2KeyColumnIgnoreDiacritics);
	}

	public String getTable2ColumnPrefix() {
		return (table2ColumnPrefixField != null ? table2ColumnPrefixField.getText() : table2ColumnPrefix);
	}

	public void setTable2ColumnPrefix(String table2ColumnPrefix) {
		this.table2ColumnPrefix = table2ColumnPrefix;
		if(table2ColumnPrefixField != null)
			table2ColumnPrefixField.setText(table2ColumnPrefix);
	}

	public String getTable2ColumnSuffix() {
		return (table2ColumnSuffixField != null ? table2ColumnSuffixField.getText() : table2ColumnSuffix);
	}

	public void setTable2ColumnSuffix(String table2ColumnSuffix) {
		this.table2ColumnSuffix = table2ColumnSuffix;
		if(table2ColumnSuffixField != null)
			table2ColumnSuffixField.setText(table2ColumnSuffix);
	}

	public boolean isInterleaveColumns() {
		return (this.interleaveColumnsBox != null ? this.interleaveColumnsBox.isSelected() : this.interleaveColumns);
	}

	public void setInterleaveColumns(boolean interleaveColumns) {
		this.interleaveColumns = interleaveColumns;
		if(this.interleaveColumnsBox != null)
			this.interleaveColumnsBox.setSelected(interleaveColumns);
	}

	public int getTable1ColumnRatio() {
		return (this.table1ColumnRatioField != null ? Integer.parseInt(table1ColumnRatioField.getText()) : table1ColumnRatio);
	}

	public void setTable1ColumnRatio(int table1ColumnRatio) {
		this.table1ColumnRatio = table1ColumnRatio;
		if(this.table1ColumnRatioField != null)
			this.table1ColumnRatioField.setText(Integer.toString(table1ColumnRatio));
	}

	public int getTable2ColumnRatio() {
		return (this.table2ColumnRatioField != null ? Integer.parseInt(table2ColumnRatioField.getText()) : table2ColumnRatio);
	}

	public void setTable2ColumnRatio(int table2ColumnRatio) {
		this.table2ColumnRatio = table2ColumnRatio;
		if(this.table2ColumnRatioField != null)
			this.table2ColumnRatioField.setText(Integer.toString(table2ColumnRatio));
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();

		retVal.setProperty("table1KeyColumn", getTable1KeyColumn());
		retVal.setProperty("table1KeyColumnCaseSensitive", Boolean.toString(getTable1KeyColumnCaseSensitive()));
		retVal.setProperty("table1KeyColumnIgnoreDiacritics", Boolean.toString(getTable1KeyColumnIgnoreDiacritics()));
		retVal.setProperty("table1ColumnPrefix", getTable1ColumnPrefix());
		retVal.setProperty("table1ColumnSuffix", getTable1ColumnSuffix());

		retVal.setProperty("table2KeyColumn", getTable2KeyColumn());
		retVal.setProperty("table2KeyColumnCaseSensitive", Boolean.toString(getTable2KeyColumnCaseSensitive()));
		retVal.setProperty("table2KeyColumnIgnoreDiacritics", Boolean.toString(getTable2KeyColumnIgnoreDiacritics()));
		retVal.setProperty("table2ColumnPrefix", getTable2ColumnPrefix());
		retVal.setProperty("table2ColumnSuffix", getTable2ColumnSuffix());

		retVal.setProperty("keyColumnName", getKeyColumnName());
		retVal.setProperty("interleaveColumns", Boolean.toString(isInterleaveColumns()));
		retVal.setProperty("table1ColumnRatio", Integer.toString(getTable1ColumnRatio()));
		retVal.setProperty("table2ColumnRatio", Integer.toString(getTable2ColumnRatio()));

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setTable1KeyColumn(properties.getProperty("table1KeyColumn", ""));
		setTable1KeyColumnCaseSensitive(
				Boolean.parseBoolean(properties.getProperty("table1KeyColumnCaseSensitive", "false")));
		setTable1KeyColumnIgnoreDiacritics(
				Boolean.parseBoolean(properties.getProperty("table1KeyColumnIgnoreDiacritics", "true")));
		setTable1ColumnPrefix(properties.getProperty("table1ColumnPrefix", ""));
		setTable1ColumnSuffix(properties.getProperty("table1ColumnSuffix", ""));

		setTable2KeyColumn(properties.getProperty("table2KeyColumn", ""));
		setTable2KeyColumnCaseSensitive(
				Boolean.parseBoolean(properties.getProperty("table2KeyColumnCaseSensitive", "false")));
		setTable2KeyColumnIgnoreDiacritics(
				Boolean.parseBoolean(properties.getProperty("table2KeyColumnIgnoreDiacritics", "true")));
		setTable2ColumnPrefix(properties.getProperty("table2ColumnPrefix", ""));
		setTable2ColumnSuffix(properties.getProperty("table2ColumnSuffix", ""));

		setKeyColumnName(properties.getProperty("keyColumnName", ""));

		setInterleaveColumns(Boolean.parseBoolean(properties.getProperty("interleaveColumns", "true")));
		setTable1ColumnRatio(Integer.parseInt(properties.getProperty("table1ColumnRatio", "1")));
		setTable2ColumnRatio(Integer.parseInt(properties.getProperty("table2ColumnRatio", "1")));
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table1 = (DefaultTableDataSource)context.get(table1Input);
		final DefaultTableDataSource table2 = (DefaultTableDataSource)context.get(table2Input);

		DefaultTableDataSource outputTable = new DefaultTableDataSource();

		String[] table1KeyColNames = getTable1KeyColumn().split(",");
		int[] table1KeyCols = new int[table1KeyColNames.length];
		for(int i = 0; i < table1KeyColNames.length; i++) {
			int table1ColIdx = table1.getColumnIndex(table1KeyColNames[i].trim());
			if(table1ColIdx < 0)
				throw new ProcessingException(null, String.format("Column '%s' not found", table1KeyColNames[i]));
			table1KeyCols[i] = table1ColIdx;
		}
		// collect unique column names from each table
		List<String> table1Columns = new ArrayList<>();
		for(int col = 0; col < table1.getColumnCount(); col++) {
			table1Columns.add(table1.getColumnTitle(col));
		}
		for(String col:table1KeyColNames) table1Columns.remove(col);

		String[] table2KeyColNames = getTable2KeyColumn().split(",");
		int[] table2KeyCols = new int[table2KeyColNames.length];
		for(int i = 0; i < table2KeyColNames.length; i++) {
			int table2ColIdx = table2.getColumnIndex(table2KeyColNames[i].trim());
			if(table2ColIdx < 0)
				throw new ProcessingException(null, String.format("Column '%s' not found", table2KeyColNames[i]));
			table2KeyCols[i] = table2ColIdx;
		}
		// collect unique column names from each table
		List<String> table2Columns = new ArrayList<>();
		for(int col = 0; col < table2.getColumnCount(); col++) {
			if(Arrays.asList(table2KeyCols).contains(col)) continue;
			table2Columns.add(table2.getColumnTitle(col));
		}
		for(String col:table2KeyColNames) table2Columns.remove(col);

		if(table1KeyCols.length == 0 && table2KeyCols.length == 0) {
			outputTable = table1;
		} else if(table1KeyCols.length == 0 && table2KeyCols.length > 0) {
			outputTable = table2;
		} else if(table1KeyCols.length > 0 && table2KeyCols.length == 0) {
			outputTable = table1;
		} else {
			// collect unique row keys from both tables
			Set<RowKey> rowKeys = new LinkedHashSet<>();
			for(int row = 0; row < table1.getRowCount(); row++) {
				checkCanceled();
				String[] key = new String[table1KeyCols.length];
				Object[] keyVals = new Object[table1KeyCols.length];
				for(int i = 0; i < table1KeyCols.length; i++) {
					Object tableVal = table1.getValueAt(row, table1KeyCols[i]);
					String rowKey = TableUtils.objToString(tableVal, getTable1KeyColumnIgnoreDiacritics());
					if(getTable1KeyColumnCaseSensitive())
						rowKey = rowKey.toLowerCase();
					key[i] = rowKey;
					keyVals[i] = tableVal;
				}

				rowKeys.add(new RowKey(key, keyVals));
			}
			for(int row = 0; row < table2.getRowCount(); row++) {
				checkCanceled();
				String[] key = new String[table2KeyCols.length];
				Object[] keyVals = new Object[table2KeyCols.length];
				for(int i = 0; i < table2KeyCols.length; i++) {
					Object tableVal = table2.getValueAt(row, table2KeyCols[i]);
					String rowKey = TableUtils.objToString(tableVal, getTable2KeyColumnIgnoreDiacritics());
					if(getTable2KeyColumnCaseSensitive())
						rowKey = rowKey.toLowerCase();
					key[i] = rowKey;
					keyVals[i] = tableVal;
				}

				rowKeys.add(new RowKey(key, keyVals));
			}

			// create output table
			int numKeyCols = Math.max(table1KeyCols.length, table2KeyCols.length);
			int numCols = numKeyCols + table1Columns.size() + table2Columns.size();
			for(RowKey rowKey:rowKeys) {
				checkCanceled();

				int col = 0;
				Object rowData[] = new Object[numCols];
				for(int i = 0; i < numKeyCols && i < rowKey.vals.length; i++) {
					rowData[col++] = rowKey.vals[i];
				}

				if(isInterleaveColumns()) {
					int table1ColIdx = 0;
					int table2ColIdx = 0;
					while(col < numCols) {
						for(int table1ColNum = 0; table1ColNum < getTable1ColumnRatio(); table1ColNum++) {
							if(table1ColIdx < table1Columns.size()) {
								int colIdx = table1.getColumnIndex(table1Columns.get(table1ColIdx));
								Object table1Val = table1.getValueAt(
										table1KeyCols, rowKey.getKeys(), table1Columns.get(table1ColIdx++),
										getTable1KeyColumnIgnoreDiacritics(), getTable1KeyColumnCaseSensitive());
								if(table1Val == null) {
									Class<?> colType = table1.inferColumnType(colIdx);
									if(colType != null && colType != Object.class) {
										try {
											if(Number.class.isAssignableFrom(colType)) {
												try {
													Constructor<?> numberCtr =
															colType.getConstructor( String.class );
													table1Val = numberCtr.newInstance( "0" );
												} catch (NoSuchMethodException | InvocationTargetException e) {
													LOGGER.warn( e.getLocalizedMessage(), e);
												}
											} else {
												table1Val = colType.newInstance();
											}
										} catch (InstantiationException | IllegalAccessException e) {
											LOGGER.warn( e.getLocalizedMessage(), e);
										}
									}
								}
								rowData[col++] = (table1Val != null ? table1Val : "");
							}
						}
						for(int table2ColNum = 0; table2ColNum < getTable2ColumnRatio(); table2ColNum++) {
							if(table2ColIdx < table2Columns.size()) {
								int colIdx = table2.getColumnIndex(table2Columns.get(table2ColIdx));
								Object table2Val = table2.getValueAt(
										table2KeyCols, rowKey.getKeys(), table2Columns.get(table2ColIdx++),
										getTable2KeyColumnIgnoreDiacritics(), getTable2KeyColumnCaseSensitive());
								if(table2Val == null) {
									Class<?> colType = table2.inferColumnType(colIdx);
									if(colType != null && colType != Object.class) {
										try {
											if(Number.class.isAssignableFrom(colType)) {
												try {
													Constructor<?> numberCtr =
															colType.getConstructor( String.class );
													table2Val = numberCtr.newInstance( "0" );
												} catch (NoSuchMethodException | InvocationTargetException e) {
													LOGGER.warn( e.getLocalizedMessage(), e);
												}
											} else {
												table2Val = colType.newInstance();
											}
										} catch (InstantiationException | IllegalAccessException e) {
											LOGGER.warn( e.getLocalizedMessage(), e);
										}
									}
								}
								rowData[col++] = (table2Val != null ? table2Val : "");
							}
						}
					}
				} else {
					for(String colName:table1Columns) {
						Object table1Val = table1.getValueAt(
								table1KeyCols, rowKey.getKeys(), colName);
						rowData[col++] = (table1Val != null ? table1Val : "");
					}
					for(String colName:table2Columns) {
						Object table2Val = table2.getValueAt(
								table2KeyCols, rowKey.getKeys(), colName);
						rowData[col++] = (table2Val != null ? table2Val : "");
					}
				}
				outputTable.addRow(rowData);
			}

			// setup column names
			int col = 0;
			final String keyColNames[] = getKeyColumnName().split(",");
			for(int i = 0; i < numKeyCols; i++) {
				String colTitle =
						(i < keyColNames.length ? keyColNames[i] : "");
				outputTable.setColumnTitle(col++, colTitle);
			}
			if(isInterleaveColumns()) {
				int table1ColIdx = 0;
				int table2ColIdx = 0;
				while(col < numCols) {
					for(int table1ColNum = 0; table1ColNum < getTable1ColumnRatio(); table1ColNum++) {
						if(table1ColIdx < table1Columns.size()) {
							String table1ColName = table1Columns.get(table1ColIdx++);
							String colName =
									getTable1ColumnPrefix() + table1ColName + getTable1ColumnSuffix();
							outputTable.setColumnTitle(col++, colName);
						}
					}
					for(int table2ColNum = 0; table2ColNum < getTable2ColumnRatio(); table2ColNum++) {
						if(table2ColIdx < table2Columns.size()) {
							String table2ColName = table2Columns.get(table2ColIdx++);
							String colName =
									getTable2ColumnPrefix() + table2ColName + getTable2ColumnSuffix();
							outputTable.setColumnTitle(col++, colName);
						}
					}
				}
			} else {
				for(String colName:table1Columns) {
					String newColName =
							getTable1ColumnPrefix() + colName + getTable1ColumnSuffix();
					outputTable.setColumnTitle(col++, newColName);
				}
				for(String colName:table2Columns) {
					String newColName =
							getTable2ColumnPrefix() + colName + getTable2ColumnSuffix();
					outputTable.setColumnTitle(col++, newColName);
				}
			}
		}

		context.put(tableOutput, outputTable);
	}

	private class RowKey {

		private String[] keys;

		private Object[] vals;

		public RowKey(String[] keys, Object[] vals) {
			this.keys = keys;
			this.vals = vals;
		}

		public String[] getKeys() {
			return this.keys;
		}

		public Object[] getVals() {
			return this.vals;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof RowKey)) return false;
			RowKey rowkey = (RowKey)obj;
			if(rowkey.keys.length != keys.length) return false;
			boolean retVal = true;
			for(int i = 0; i < keys.length; i++) {
				retVal &= keys[i].equals(rowkey.keys[i]);
			}
			return retVal;
		}

		final int somePrimes[] = new int[] { 991, 997, 1003, 1013, 5881, 104623, 104701 };
		@Override
		public int hashCode() {
			int retVal = 0;
			for(int i = 0; i < keys.length; i++) {
				retVal ^= somePrimes[i % somePrimes.length] * keys[i].hashCode();
			}
			return retVal;
		}

		@Override
		public String toString() {
			return Arrays.toString(this.keys);
		}

	}

}
