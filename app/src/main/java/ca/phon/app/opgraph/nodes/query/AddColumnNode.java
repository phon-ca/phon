package ca.phon.app.opgraph.nodes.query;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.ui.text.PromptedTextField;

/**
 * Add a new column to the given table using
 * a user-entered script.
 * 
 */
@OpNodeInfo(
		category="Report",
		description="Add a new column to the given table using a script.",
		name="Add Column",
		showInLibrary=true
)
public class AddColumnNode extends TableScriptNode {

	private final static String DEFAULT_SCRIPT = "/*\n" + 
			"params = {label, \"Add a new column to the input table using javascript.\", \"<html><b>Add column to table</b></html>\"}\n" + 
			";\n" + 
			"*/\n" + 
			"\n" + 
			"function getRowValue(table, row) {\n" + 
			"	return new String();\n" + 
			"}\n" + 
			"";
	
	// settings
	private String columnName = "NewColumn";
	
	// UI
	private PromptedTextField columnNameField;
	
	public AddColumnNode() {
		super(DEFAULT_SCRIPT);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();
		
		final TableDataSource table = super.getInputTable(context);
		
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();
		
		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);
			
			for(int row = 0; row < table.getRowCount(); row++) {
				final Map<String, Object> rowData = new LinkedHashMap<>();
				
				for(int col = 0; col < table.getColumnCount(); col++) {
					rowData.put(table.getColumnTitle(col), table.getValueAt(row, col));
				}
				
				Object newVal = scriptContext.callFunction(scope, "getRowValue", 
						table, row);
				if(newVal instanceof NativeJavaObject) {
					newVal = ((NativeJavaObject)newVal).unwrap();
				}
				
				rowData.put(getColumnName(), (newVal != null ?
						newVal : new String()));
				
				// add row to outputtable
				outputTable.addRow(rowData.values().toArray());
			}
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e);
		}
		
		for(int i = 0; i < table.getColumnCount(); i++) 
			outputTable.setColumnTitle(i, table.getColumnTitle(i));
		outputTable.setColumnTitle(outputTable.getColumnCount()-1, getColumnName());
		
		context.put(tableOutput, outputTable);
	}
	
	@Override
	protected JPanel createSettingsPanel() {
		JPanel retVal = super.createSettingsPanel();
		
		columnNameField = new PromptedTextField("Enter new column name");
		columnNameField.setText(this.columnName);
		columnNameField.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Column name"), columnNameField.getBorder()));
		retVal.add(columnNameField, BorderLayout.NORTH);
		
		return retVal;
	}
	
	public String getColumnName() {
		return (columnNameField != null ? columnNameField.getText() : columnName);
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = super.getSettings();
		
		retVal.setProperty("column", getColumnName());
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		super.loadSettings(properties);
		
		if(properties.containsKey("column")) {
			this.columnName = properties.getProperty("column");
			if(columnNameField != null)
				columnNameField.setText(properties.getProperty("column"));
		}
	}

}
