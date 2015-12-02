package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.ui.text.PromptedTextField;

@OpNodeInfo(
		name="Extract Columns",
		description="Extract columns from table",
		category="Report"
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
			for(int j = 0; j < rowData.length; j++) {
				final int colIdx = columnIndices[j];
				rowData[j] = 
						(colIdx >= 0 && colIdx < table.getColumnCount() ? table.getValueAt(i, colIdx) : null);
			}
			outputTable.addRow(rowData);
		}
		
		for(int i = 0; i < columnIndices.length; i++) {
			final int colIdx = columnIndices[i];
			outputTable.setColumntTitle(i, table.getColumnTitle(colIdx));
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
