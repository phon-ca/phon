/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptException;
import javax.swing.JPanel;

import org.mozilla.javascript.Scriptable;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.general.script.InputFields;
import ca.gedge.opgraph.nodes.general.script.OutputFields;
import ca.phon.app.query.ScriptPanel;
import ca.phon.plugin.PluginManager;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Base class for script operations on tables.  This node looks for
 * the function 'tableOp(context, table)' in the user-provided script.
 * Output table will be same object as input table.
 * 
 */
@OpNodeInfo(category="Report", description="Custom script for table input", name="Table Script", showInLibrary=true)
public class TableScriptNode extends TableOpNode implements NodeSettings {
	
	private final static Logger LOGGER = Logger.getLogger(TableScriptNode.class.getName());
	
	private final static String SCRIPT_TEMPLATE =
			"function tableOp(context, table) {\n}\n";

	// script
	private PhonScript script;
	
	// UI
	private JPanel settingsPanel;
	private ScriptPanel scriptPanel = new ScriptPanel();
	
	public TableScriptNode() {
		this("");
	}

	public TableScriptNode(String script) {
		this(new BasicScript(script));
		putExtension(NodeSettings.class, this);
	}
	
	public TableScriptNode(PhonScript script) {
		super();
		this.script = script;
		addQueryLibrary();
	}
	
	public PhonScript getScript() {
		return this.script;
	}
	
	public ScriptPanel getScriptPanel() {
		return this.scriptPanel;
	}
	
	/**
	 * Make query library functions available to scripts.
	 * 
	 */
	private void addQueryLibrary() {
		final ClassLoader cl = PluginManager.getInstance();
		Enumeration<URL> libUrls;
		try {
			libUrls = cl.getResources("ca/phon/query/script/");
			while(libUrls.hasMoreElements()) {
				final URL url = libUrls.nextElement();
				try {
					final URI uri = url.toURI();
					script.addRequirePath(uri);
				} catch (URISyntaxException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
	}
	
	/**
	 * Reload the input/output fields from the script. 
	 */
	private void reloadFields() {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();
		
		final List<InputField> fixedInputs =
				getInputFields().stream().filter( f -> f.isFixed() && f != ENABLED_FIELD ).collect( Collectors.toList() );
		final List<OutputField> fixedOutputs =
				getOutputFields().stream().filter( OutputField::isFixed ).collect( Collectors.toList() );
		
		removeAllInputFields();
		removeAllOutputFields();
		
		for(InputField field:fixedInputs) {
			putField(field);
		}
		for(OutputField field:fixedOutputs) {
			putField(field);
		}
		
		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);
			
			final InputFields inputFields = new InputFields(this);
			final OutputFields outputFields = new OutputFields(this);
			scriptContext.callFunction(scope, "init", inputFields, outputFields);
		} catch (PhonScriptException e) {
			LOGGER.fine(e.getLocalizedMessage());
		}
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = createSettingsPanel();
		}
		return settingsPanel;
	}
	
	protected JPanel createSettingsPanel() {
		JPanel retVal = new JPanel(new BorderLayout());

		scriptPanel = new ScriptPanel(getScript());
		retVal.add(scriptPanel, BorderLayout.CENTER);
		scriptPanel.addPropertyChangeListener(ScriptPanel.SCRIPT_PROP, e -> reloadFields() );
		
		return retVal;
	}
	
	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();
		
		retVal.setProperty("__script", getScript().getScript());
		
		try {
			final ScriptParameters scriptParams = getScript().getContext().getScriptParameters(
					getScript().getContext().getEvaluatedScope());
			for(ScriptParam param:scriptParams) {
				if(param.hasChanged()) {
					for(String paramId:param.getParamIds()) {
						retVal.setProperty(paramId, param.getValue(paramId).toString());
					}
				}
			}
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("__script")) {
			this.script = new BasicScript(properties.getProperty("__script"));
			addQueryLibrary();
			if(scriptPanel != null)
				scriptPanel.setScript(this.script);
			reloadFields();
			
			try {
				final ScriptParameters scriptParams = getScript().getContext().getScriptParameters(
						getScript().getContext().getEvaluatedScope());
				for(ScriptParam param:scriptParams) {
					for(String paramId:param.getParamIds()) {
						if(properties.containsKey(paramId)) {
							param.setValue(paramId, properties.get(paramId));
						}
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);	
			}
		}
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();
		
		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);
			
			scriptContext.callFunction(scope, "tableOp", context, table);
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		context.put(tableOutput, table);
	}
	
}
