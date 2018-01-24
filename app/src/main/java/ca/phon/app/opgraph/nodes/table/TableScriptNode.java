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
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.mozilla.javascript.Scriptable;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.general.script.*;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.ScriptPanel;
import ca.phon.plugin.PluginManager;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.util.resources.*;

/**
 * Base class for script operations on tables.  This node looks for
 * the function 'tableOp(context, table)' in the user-provided script.
 * Output table will be same object as input table.
 *
 */
@OpNodeInfo(category="Table", description="Custom script for table input", name="Table Script", showInLibrary=true)
public class TableScriptNode extends TableOpNode implements NodeSettings {

	private final static Logger LOGGER = Logger.getLogger(TableScriptNode.class.getName());

	private final static String TABLE_SCRIPT_RESOURCE_FILE = "ca/phon/app/opgraph/nodes/table/table_scripts";

	private final static String SCRIPT_TEMPLATE =
			"function tableOp(context, table) {\n}\n";

	private InputField paramsInputField = new InputField("parameters", "Map of query parameters, these will override query settings.",
			true, true, Map.class);

	private OutputField paramsOutputField = new OutputField("parameters",
			"Parameters used for query, including those entered using the settings dialog", true, Map.class);

	public static ResourceLoader<URL> getTableScriptResourceLoader() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();

		// add classpath handler
		final ClassLoaderHandler<URL> handler = new ClassLoaderHandler<URL>() {

			@Override
			public URL loadFromURL(URL url) throws IOException {
				return url;
			}

		};
		handler.loadResourceFile(TABLE_SCRIPT_RESOURCE_FILE);
		retVal.addHandler(handler);

		return retVal;
	}

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
	}

	public TableScriptNode(PhonScript script) {
		super();
		this.script = script;
		addQueryLibrary();

		putField(paramsInputField);
		putField(paramsOutputField);
		reloadFields();

		putExtension(NodeSettings.class, this);
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
		script.addPackageImport("Packages.ca.phon.session");
		script.addPackageImport("Packages.ca.phon.project");
		script.addPackageImport("Packages.ca.phon.ipa");
		script.addPackageImport("Packages.ca.phon.query");
		script.addPackageImport("Packages.ca.phon.query.report");
		script.addPackageImport("Packages.ca.phon.query.report.datasource");

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

		// setup fields on temporary node
		final OpNode tempNode = new OpNode("temp", "temp", "temp") {
			@Override
			public void operate(OpContext context) throws ProcessingException {
			}
		};
		for(InputField field:fixedInputs) {
			tempNode.putField(field);
		}
		for(OutputField field:fixedOutputs) {
			tempNode.putField(field);
		}
		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);

			final InputFields inputFields = new InputFields(tempNode);
			final OutputFields outputFields = new OutputFields(tempNode);

			if(scriptContext.hasFunction(scope, "init", 2)) {
				scriptContext.callFunction(scope, "init", inputFields, outputFields);
			}
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, getName() + " (" + getId() + "): " + e.getLocalizedMessage(), e);
		}

		// check inputs
		final List<InputField> inputFields = new ArrayList<>(getInputFields());
		for(InputField currentInputField:inputFields) {
			final InputField tempInputField = tempNode.getInputFieldWithKey(currentInputField.getKey());
			if(tempInputField != null) {
				// copy field information
				currentInputField.setDescription(tempInputField.getDescription());
				currentInputField.setFixed(tempInputField.isFixed());
				currentInputField.setOptional(tempInputField.isOptional());
				currentInputField.setValidator(tempInputField.getValidator());
			} else {
				// remove field from node
				removeField(currentInputField);
			}
		}

		final List<String> tempInputKeys = tempNode.getInputFields()
				.stream().map( InputField::getKey ).collect( Collectors.toList() );
		// add new input fields
		for(String tempInputKey:tempInputKeys) {
			final InputField currentInput = getInputFieldWithKey(tempInputKey);
			if(currentInput == null) {
				// add new field to node
				putField(tempInputKeys.indexOf(tempInputKey), tempNode.getInputFieldWithKey(tempInputKey));
			}
		}

		// check outputs
		final List<OutputField> outputFields = new ArrayList<>(getOutputFields());
		for(OutputField currentOutputField:outputFields) {
			final OutputField tempOutputField = tempNode.getOutputFieldWithKey(currentOutputField.getKey());
			if(tempOutputField != null) {
				currentOutputField.setDescription(tempOutputField.getDescription());
				currentOutputField.setFixed(tempOutputField.isFixed());
				currentOutputField.setOutputType(tempOutputField.getOutputType());
			} else {
				removeField(currentOutputField);
			}
		}

		final List<String> tempOutputKeys = tempNode.getOutputFields()
				.stream().map( OutputField::getKey ).collect( Collectors.toList() );
		for(String tempOutputKey:tempOutputKeys) {
			final OutputField currentOutput = getOutputFieldWithKey(tempOutputKey);
			if(currentOutput == null) {
				putField(tempOutputKeys.indexOf(tempOutputKey), tempNode.getOutputFieldWithKey(tempOutputKey));
			}
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
		final PhonScriptContext ctx = phonScript.getContext();

		ScriptParameters scriptParams = new ScriptParameters();
		try {
			scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, getName() + " (" + getId() + "): " + e.getLocalizedMessage(), e);
		}

		final Map<?, ?> inputParams = (Map<?,?>)context.get(paramsInputField);
		final Map<String, Object> allParams = new LinkedHashMap<>();
		for(ScriptParam sp:scriptParams) {
			for(String paramId:sp.getParamIds()) {
				if(inputParams != null && inputParams.containsKey(paramId)) {
					sp.setValue(paramId, inputParams.get(paramId));
				}

				if(paramId.endsWith("ignoreDiacritics")
						&& context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)) {
					sp.setValue(paramId, context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION));
				}

				if(paramId.endsWith("caseSensitive")
						&& context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION)) {
					sp.setValue(paramId, context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION));
				}

				allParams.put(paramId, sp.getValue(paramId));
			}
		}

		// ensure query form validates (if available)
		if(scriptPanel != null && !scriptPanel.checkParams()) {
			throw new ProcessingException(null, getName() + " (" + getId() + "): " + "Invalid settings");
		}

		try {
			final Scriptable scope = ctx.getEvaluatedScope();
			ctx.installParams(scope);

			ctx.callFunction(scope, "tableOp", context, table);
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, getName() + " (" + getId() + "): " + e.getLocalizedMessage(), e);
		}

		context.put(tableOutput, table);
		context.put(paramsOutputField, allParams);
	}

}
