package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.script.ScriptEngineManager;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.script.BasicScript;
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
public class AddColumnNode extends TableOpNode implements NodeSettings {

	private final static String DEFAULT_SCRIPT = "function getRowValue(rowData) {\n\treturn new String();\n}\n";
	
	// settings
	private String columnName = "NewColumn";
	
	private String scriptEngineName = "Javascript";
	
	private PhonScript script = new BasicScript(DEFAULT_SCRIPT);
	
	
	// UI
	private JPanel settingsPanel;
	private PromptedTextField columnNameField;
	private JComboBox<String> scriptEngineBox;
	private JComboBox<String> cannedScriptBox;
	private RSyntaxTextArea scriptArea;
	
	public AddColumnNode() {
		super();
		putExtension(NodeSettings.class, this);
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
						Collections.unmodifiableMap(rowData));
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
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}
	
	private JPanel createSettingsPanel() {
		JPanel retVal = new JPanel();
		
		columnNameField = new PromptedTextField("Enter new column name");
		final ScriptEngineManager manager = new ScriptEngineManager();
		final List<String> scriptEngineNames = 
				manager.getEngineFactories()
					.stream()
					.map( (factory) -> factory.getEngineName() )
					.collect(Collectors.toList());
		scriptEngineBox = new JComboBox<>(scriptEngineNames.toArray(new String[0]));
		
		cannedScriptBox = new JComboBox<>();
		
		scriptArea = new RSyntaxTextArea();
		final RTextScrollPane scroller = new RTextScrollPane(scriptArea, true);
		
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		retVal.setLayout(layout);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 5, 2);
		
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		retVal.add(new JLabel("Column name:"), gbc);
		
		gbc.gridx++;
		gbc.weightx = 1.0;
		retVal.add(columnNameField, gbc);
		
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.gridy++;
		retVal.add(new JLabel("Script engine:"), gbc);
		
		gbc.gridx++;
		gbc.weightx = 1.0;
		retVal.add(scriptEngineBox, gbc);
		
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.gridy++;
		retVal.add(new JLabel("Column type:"), gbc);
		
		gbc.gridx++;
		gbc.weightx = 1.0;
		retVal.add(cannedScriptBox, gbc);
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		scroller.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Script"), 
						scroller.getBorder()));
		retVal.add(scroller, gbc);
		
		columnNameField.setText(this.columnName);
		scriptEngineBox.setSelectedItem(this.scriptEngineName);
		scriptArea.setText(this.script.getScript());
		
		return retVal;
	}
	
	public String getColumnName() {
		return (columnNameField != null ? columnNameField.getText() : columnName);
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public String getScriptEngineName() {
		return (scriptEngineBox != null ? scriptEngineBox.getSelectedItem().toString() : scriptEngineName);
	}
	
	public void setScriptEngineName(String scriptEngine) {
		this.scriptEngineName = scriptEngine;
	}
	
	public PhonScript getScript() {
		return (scriptArea != null ? new BasicScript(scriptArea.getText()) : script);
	}
	
	public void setScript(String script) {
		this.script = new BasicScript(script);
	}
	
	public void setScript(PhonScript script) {
		this.script = script;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		
		retVal.setProperty("column", getColumnName());
		retVal.setProperty("scriptEngineName", getScriptEngineName());
		retVal.setProperty("script", getScript().getScript());
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("column")) {
			this.columnName = properties.getProperty("column");
			if(columnNameField != null)
				columnNameField.setText(properties.getProperty("column"));
		}
		if(properties.containsKey("scriptEngineName")) {
			this.scriptEngineName = properties.getProperty("scriptEngineName");
			if(scriptEngineBox != null)
				scriptEngineBox.setSelectedItem(properties.getProperty("scriptEngineName"));
		}
		if(properties.containsKey("script")) {
			this.script = new BasicScript(properties.getProperty("script"));
			if(scriptArea != null)
				scriptArea.setText(properties.getProperty("script"));
		}
	}

}
