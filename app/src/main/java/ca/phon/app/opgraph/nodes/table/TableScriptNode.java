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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import ca.phon.app.script.*;
import org.apache.commons.io.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.script.*;
import ca.phon.query.report.datasource.*;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.ui.*;
import ca.phon.ui.layout.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import ca.phon.util.resources.*;

/**
 * Base class for script operations on tables.  This node looks for
 * the function 'tableOp(context, table)' in the user-provided script.
 * Output table will be same object as input table.
 *
 */
@OpNodeInfo(category="Table", description="Custom script for table input", name="Table Script", showInLibrary=true)
public class TableScriptNode extends TableOpNode implements NodeSettings, ScriptNode {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(TableScriptNode.class.getName());

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
	private JComponent settingsComponent;
	private JCheckBox debugBox;
	private ScriptPanel scriptPanel;

	public TableScriptNode() {
		this("");
	}

	public TableScriptNode(String script) {
		this(new BasicScript(script));
	}

	public TableScriptNode(PhonScript script) {
		super();
		this.script = script;
		QueryScript.setupScriptRequirements(this.script);

		putField(paramsInputField);
		putField(paramsOutputField);
		reloadFields();

		putExtension(NodeSettings.class, this);
	}

	@Override
	public PhonScript getScript() {
		return this.script;
	}

	public ScriptPanel getScriptPanel() {
		return this.scriptPanel;
	}

	@Override
	public OpNode toOpNode() {
		return this;
	}

	/**
	 * Reload the input/output fields from the script.
	 */
	@Override
	public void reloadFields() {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();

		final List<InputField> fixedInputs =
				getInputFields().stream().filter( f -> f.isFixed() && f != ENABLED_FIELD ).collect( Collectors.toList() );
		final List<OutputField> fixedOutputs =
				getOutputFields().stream().filter( f-> f.isFixed() && f != COMPLETED_FIELD ).collect( Collectors.toList() );

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
			LOGGER.error( getName() + " (" + getId() + "): " + e.getLocalizedMessage(), e);
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
		if(settingsComponent == null) {
			scriptPanel = createScriptPanel();
			settingsComponent = createSettingsPanel();
		}
		return settingsComponent;
	}

	private ScriptPanel createScriptPanel() {
		ScriptPanel retVal = new ScriptPanel(getScript());
		retVal.addPropertyChangeListener(ScriptPanel.SCRIPT_PROP, e -> reloadFields() );
		
//		if(CommonModuleFrame.getCurrentFrame() instanceof OpgraphEditor) {
//			retVal.setSwapButtonVisible(true);
//		}
		
		return retVal;		
	}
	
	protected JComponent createSettingsPanel() {
		JPanel retVal = new JPanel();
		
		retVal.setLayout(new BorderLayout());
		JScrollPane scriptScroller = new JScrollPane(scriptPanel);
		scriptScroller.getViewport().setBackground(scriptPanel.getBackground());
		retVal.add(scriptScroller, BorderLayout.CENTER);

		if(shouldShowDebug()) {
			debugBox = new JCheckBox("Debug");
			debugBox.setSelected(false);
			debugBox.setToolTipText("Show debugger when executing this node");

			
			final JComponent editor = ScriptEditorFactory.createEditorComponentForScript(getScript());
			final Action act = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {}
			};
			act.putValue(Action.NAME, "Edit script");
			act.putValue(Action.SHORT_DESCRIPTION, "Show script editor");
			act.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/edit", IconSize.SMALL));
			act.putValue(DropDownButton.BUTTON_POPUP, editor);
			act.putValue(DropDownButton.ARROW_ICON_GAP, 2);
			act.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);

			retVal.add(ButtonBarBuilder.buildOkBar(debugBox), BorderLayout.SOUTH);
		}
		
		return retVal;
	}
	
	private boolean shouldShowDebug() {
		return (CommonModuleFrame.getCurrentFrame() instanceof OpgraphEditor
				|| PrefHelper.getBoolean("phon.debug", Boolean.FALSE));
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();

		final TableScriptName scriptName = getScript().getExtension(TableScriptName.class);
		if(scriptName != null) {
			retVal.setProperty("__scriptName", scriptName.name);
		} else {
			retVal.setProperty("__script", getScript().getScript());
		}
		
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
			LOGGER.error( e.getLocalizedMessage(), e);
		}


		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("__script")) {
			this.script = new BasicScript(properties.getProperty("__script"));
			if(scriptPanel != null)
				scriptPanel.setScript(this.script);
		} else if(properties.containsKey("__scriptName")) {
			final String scriptName = properties.getProperty("__scriptName");
			final ResourceLoader<URL> stockScripts = getTableScriptResourceLoader();
			final Optional<URL> scriptURL = StreamSupport.stream(stockScripts.spliterator(), true)
				.filter( (url) -> {
					try {
						final String name = FilenameUtils.getBaseName(
								URLDecoder.decode(url.getFile(), "UTF-8"));
						return name.equals(scriptName);
					} catch (UnsupportedEncodingException e1) {
						LOGGER.error( e1.getLocalizedMessage(), e1);
					}
					return false;
				} )
				.findFirst();
			if(scriptURL.isPresent()) {
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(scriptURL.get().openStream(), "UTF-8"))) {
					StringBuffer buffer = new StringBuffer();
					String line = null;
					while((line = reader.readLine()) != null) {
						buffer.append(line).append("\n");
					}
					final PhonScript script = new BasicScript(buffer.toString());
					this.script = script;
					
					this.script.putExtension(TableScriptName.class, new TableScriptName(scriptName));
				} catch (IOException e) {
					LOGGER.warn( e.getLocalizedMessage(), e);
				}
			} else {
				LOGGER.warn("Unable to locate table script: " + scriptName);
			}
		}
		QueryScript.setupScriptRequirements(getScript());
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
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		boolean isStepInto = (context.containsKey("__stepInto") ? Boolean.valueOf(context.get("__stepInto").toString()) : false);
		
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);

		final PhonScript phonScript = getScript();
		PhonScriptContext ctx = phonScript.getContext();

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
			throw new ProcessingException(null, getName() + " (" + getId() + "): " + "Invalid settings");
		}

		if(isStepInto || (debugBox != null && debugBox.isSelected())) {
			try {
				org.mozilla.javascript.tools.debugger.Main debugger = Main.mainEmbedded(getName());
				debugger.setBreakOnEnter(false);
				debugger.setBreakOnExceptions(true);
				
				final ScriptParameters params = ctx.getScriptParameters(ctx.getEvaluatedScope());
				
				// we need to reset the context to activate debugging
				script.resetContext();
				ctx = script.getContext();
				
				final Context jsctx = ctx.enter();
				final ScriptableObject debugScope = jsctx.initStandardObjects();
				jsctx.setOptimizationLevel(-1);
				debugger.attachTo(jsctx.getFactory());
				debugger.setScope(debugScope);
				ctx.exit();
				
				final Scriptable runScope = ctx.getEvaluatedScope(debugScope);
				final ScriptParameters newParams = ctx.getScriptParameters(runScope);
				ScriptParameters.copyParams(params, newParams);
				
				debugger.setExitAction(new Runnable() {
					
					@Override
					public void run() {
						debugger.detach();
						debugger.setVisible(false);
					}
					
				});
				// break on entering main query script
				debugger.doBreak();
				debugger.setSize(500, 600);
				debugger.setVisible(true);
				
				ctx.installParams(runScope);
				ctx.callFunction(runScope, "tableOp", context, table);
			} catch (PhonScriptException e) {
				LogUtil.severe(e);
			}
		} else {
			try {
				final Scriptable scope = ctx.getEvaluatedScope();
				ctx.installParams(scope);
	
				ctx.callFunction(scope, "tableOp", context, table);
			} catch (PhonScriptException e) {
				throw new ProcessingException(null, getName() + " (" + getId() + "): " + e.getLocalizedMessage(), e);
			}
		}
		context.put(tableOutput, table);
		context.put(paramsOutputField, allParams);
	}

	static class TableScriptName {
		String name;
		
		public TableScriptName() {
			this("");
		}
		
		public TableScriptName(String name) {
			this.name = name;
		}
	}
	
}
