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
package ca.phon.app.opgraph.nodes;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.ScriptPanel;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.opgraph.nodes.general.script.InputFields;
import ca.phon.opgraph.nodes.general.script.OutputFields;
import ca.phon.plugin.PluginManager;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.ui.CommonModuleFrame;

@OpNodeInfo(
		name="Script",
		category="General",
		description="Generic script node with optional parameter setup.",
		showInLibrary=true
)
public class PhonScriptNode extends OpNode implements NodeSettings {

	private final static Logger LOGGER = Logger.getLogger(PhonScriptNode.class.getName());

	private PhonScript script;

	private ScriptPanel scriptPanel;

//	private InputField showDebuggerInputField = new InputField("showDebugger", "Show debug UI when executing this node",
//			true, true, Boolean.class);

	private InputField paramsInputField = new InputField("parameters", "Map of script parameters, these will override node settings.",
			true, true, Map.class);


	private OutputField paramsOutputField = new OutputField("parameters",
			"Parameters used for script, including those entered using the node settings dialog", true, Map.class);

	public OutputField scriptOutputField = new OutputField("script",
			"Script object", true, PhonScript.class);

	public PhonScriptNode() {
		this("");
	}

	public PhonScriptNode(String text) {
		this(new BasicScript(text));
	}

	public PhonScriptNode(PhonScript script) {
		super();

		this.script = script;
		addQueryLibrary();

//		putField(showDebuggerInputField);
		putField(paramsInputField);
		putField(scriptOutputField);
		putField(paramsOutputField);

		reloadFields();

		putExtension(NodeSettings.class, this);
	}

	private void reloadFields() {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();

		final List<InputField> fixedInputs =
				getInputFields().stream().filter( f -> f.isFixed() && f != ENABLED_FIELD ).collect( Collectors.toList() );
		final List<OutputField> fixedOutputs =
				getOutputFields().stream().filter( f -> f.isFixed() && f != COMPLETED_FIELD ).collect( Collectors.toList() );

//		removeAllInputFields();
//		removeAllOutputFields();

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
		for(InputField currentInputField:getInputFields().toArray(new InputField[0])) {
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
		for(OutputField currentOutputField:getOutputFields().toArray(new OutputField[0])) {
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
	public void operate(OpContext context) throws ProcessingException {
//		final boolean showDebugger = (context.get(showDebuggerInputField) != null ? (boolean)context.get(showDebuggerInputField) : false);

		final PhonScript phonScript = getScript();
		PhonScriptContext ctx = phonScript.getContext();

		ScriptParameters scriptParams = new ScriptParameters();
		try {
			scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e);
		}

		final Map<?, ?> inputParams = (Map<?,?>)context.get(paramsInputField);
		final Map<String, Object> allParams = new LinkedHashMap<>();
		for(ScriptParam sp:scriptParams) {
			for(String paramId:sp.getParamIds()) {
				if(inputParams != null && inputParams.containsKey(paramId)) {
					sp.setValue(paramId, inputParams.get(paramId));
				}

				if(paramId.endsWith("ignoreDiacritics")
						&& context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)
						&& !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")) {
					sp.setValue(paramId, context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION));
				}

				if(paramId.endsWith("caseSensitive")
						&& context.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION)
						&& !context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default")) {
					sp.setValue(paramId, context.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION));
				}

				allParams.put(paramId, sp.getValue(paramId));
			}
		}

		// ensure query form validates (if available)
		if(scriptPanel != null && !scriptPanel.checkParams()) {
			throw new ProcessingException(null, "Invalid settings");
		}

		try {
			final Scriptable scope = ctx.getEvaluatedScope();
			ctx.installParams(scope);

			if(ctx.hasFunction(scope, "run", 1)) {
//				if(showDebugger) {
//					// reset context
//					((BasicScript)phonScript).resetContext();
//					ctx = phonScript.getContext();
//
//					final Main debugger = Main.mainEmbedded("Debugger : " + getName());
//					debugger.setBreakOnEnter(true);
//					debugger.setBreakOnExceptions(true);
//
//					final Context jsContext = ctx.enter();
//					jsContext.setOptimizationLevel(-1);
//					final ScriptableObject debugScope = jsContext.initStandardObjects();
//
//					debugger.attachTo(jsContext.getFactory());
//					debugger.setScope(debugScope);
//					ctx.exit();
//
//					final ScriptParameters newParams = ctx.getScriptParameters(ctx.getEvaluatedScope(debugScope));
//					ScriptParameters.copyParams(scriptParams, newParams);
//
//					debugger.setExitAction(new Runnable() {
//
//						@Override
//						public void run() {
//							debugger.detach();
//							debugger.setVisible(false);
//						}
//
//					});
//					// break on entering main query script
//					debugger.doBreak();
//					debugger.pack();
//					debugger.setVisible(true);
////					debugger.go();
//				}
				ctx.callFunction(scope, "run", context);
			}
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new ProcessingException(null, e.getLocalizedMessage(), e);
		}

		context.put(scriptOutputField, getScript());
		context.put(paramsOutputField, allParams);
	}

	public PhonScript getScript() {
		return this.script;
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(this.scriptPanel == null) {
			this.scriptPanel = new ScriptPanel(getScript());
			scriptPanel.addPropertyChangeListener(ScriptPanel.SCRIPT_PROP, e -> reloadFields() );
			
			if(CommonModuleFrame.getCurrentFrame() instanceof OpgraphEditor) {
				this.scriptPanel.setSwapButtonVisible(true);
			}
		}
		return this.scriptPanel;
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
		} catch (PhonScriptException | RhinoException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}

		return retVal;
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

}
