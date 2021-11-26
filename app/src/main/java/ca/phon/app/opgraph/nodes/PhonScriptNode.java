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
package ca.phon.app.opgraph.nodes;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.query.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.components.canvas.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.script.*;
import ca.phon.plugin.*;
import ca.phon.query.script.params.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.ui.*;
import ca.phon.ui.layout.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

@OpNodeInfo(
		name="PhonScript",
		category="Scripting",
		description="PhonScript node with optional parameter setup and form.",
		showInLibrary=true
)
public class PhonScriptNode extends OpNode implements NodeSettings, CanvasContextMenuExtension, ScriptNode {

	private PhonScript script;
	
	private JComponent settingsComponent;

	private JCheckBox debugBox;
	
	private ScriptPanel scriptPanel;

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

		putField(paramsInputField);
		putField(scriptOutputField);
		putField(paramsOutputField);

		try {
			reloadFields();
		} catch (PhonScriptException e) {
			LogUtil.warning(e);
		}

		putExtension(NodeSettings.class, this);
		putExtension(CanvasContextMenuExtension.class, this);
	}

	@Override
	public OpNode getOpNode() {
		return this;
	}

	@Override
	public ScriptPanel getScriptPanel() {
		return scriptPanel;
	}

	@Override
	public void reloadFields() throws PhonScriptException {
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();

		final List<InputField> fixedInputs =
				getInputFields().stream().filter( f -> f.isFixed() && f != ENABLED_FIELD ).collect( Collectors.toList() );
		final List<OutputField> fixedOutputs =
				getOutputFields().stream().filter( f -> f.isFixed() && f != COMPLETED_FIELD ).collect( Collectors.toList() );

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

		final Scriptable scope = scriptContext.getEvaluatedScope();
		scriptContext.installParams(scope);

		final InputFields inputFields = new InputFields(tempNode);
		final OutputFields outputFields = new OutputFields(tempNode);

		if(scriptContext.hasFunction(scope, "init", 2)) {
			scriptContext.callFunction(scope, "init", inputFields, outputFields);
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
		boolean isStepInto = (context.containsKey("__stepInto") ? Boolean.valueOf(context.get("__stepInto").toString()) : false);
		
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

				if(paramId.endsWith(DiacriticOptionsScriptParam.IGNORE_DIACRITICS_PARAM)
						&& context.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)
						&& !context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")) {
					sp.setValue(paramId, context.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION));
				} else if(paramId.endsWith(DiacriticOptionsScriptParam.SELECTION_MODE_PARAM)
						&& context.get(NodeWizard.ONLYOREXCEPT_GLOBAL_OPTION) != null) {
					boolean onlyOrExcept = Boolean.valueOf(context.get(NodeWizard.ONLYOREXCEPT_GLOBAL_OPTION).toString());
					sp.setValue(paramId, onlyOrExcept ? DiacriticOptionsScriptParam.SelectionMode.ONLY.toString() : DiacriticOptionsScriptParam.SelectionMode.EXCEPT.toString());
				} else if(paramId.endsWith(DiacriticOptionsScriptParam.SELECTED_DIACRITICS_PARAM)
						&& context.get(NodeWizard.SELECTED_DIACRITICS_GLOBAL_OPTION) != null) {
					sp.setValue(paramId, context.get(NodeWizard.SELECTED_DIACRITICS_GLOBAL_OPTION));
				} else if(paramId.endsWith("caseSensitive")
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
		
		// call debugger if stepping into a javascript node
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
				if(ctx.hasFunction(runScope, "run", 1)) {
					ctx.callFunction(runScope, "run", context);
				}
			} catch (PhonScriptException e) {
				LogUtil.severe(e);
			}
		} else {
			try {
				final Scriptable scope = ctx.getEvaluatedScope();
				ctx.installParams(scope);
	
				if(ctx.hasFunction(scope, "run", 1)) {
					ctx.callFunction(scope, "run", context);
				}
			} catch (PhonScriptException e) {
				LogUtil.severe( e.getLocalizedMessage(), e);
				throw new ProcessingException(null, e.getLocalizedMessage(), e);
			}
	
		}
		context.put(scriptOutputField, getScript());
		context.put(paramsOutputField, allParams);
	}

	@Override
	public PhonScript getScript() {
		return this.script;
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
		retVal.addPropertyChangeListener(ScriptPanel.SCRIPT_PROP, e -> {
			try {
				reloadFields();
			} catch (PhonScriptException ex) {
				LogUtil.warning(ex);
			}
		});
		
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
			LogUtil.severe( e.getLocalizedMessage(), e);
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
					LogUtil.severe( e.getLocalizedMessage(), e);
				}
			}
		} catch (IOException e1) {
			LogUtil.severe( e1.getLocalizedMessage(), e1);
		}
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("__script")) {
			this.script = new BasicScript(properties.getProperty("__script"));
			addQueryLibrary();
			if(scriptPanel != null)
				scriptPanel.setScript(this.script);
			try {
				reloadFields();
			} catch (PhonScriptException e) {
				LogUtil.warning(e);
				return;
			}

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
				LogUtil.severe( e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public void addContextMenuItems(JPopupMenu jPopupMenu, GraphDocument graphDocument, MouseEvent mouseEvent) {

	}
}
