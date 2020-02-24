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
package ca.phon.app.opgraph.nodes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.ScriptEditorFactory;
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
import ca.phon.query.script.params.DiacriticOptionsScriptParam;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.ui.ButtonPopup;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

@OpNodeInfo(
		name="Script",
		category="General",
		description="Generic script node with optional parameter setup.",
		showInLibrary=true
)
public class PhonScriptNode extends OpNode implements NodeSettings {

	private PhonScript script;
	
	private JComponent settingsComponent;

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
			LogUtil.severe( getName() + " (" + getId() + "): " + e.getLocalizedMessage(), e);
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

		context.put(scriptOutputField, getScript());
		context.put(paramsOutputField, allParams);
	}

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
		retVal.addPropertyChangeListener(ScriptPanel.SCRIPT_PROP, e -> reloadFields() );
		
		return retVal;		
	}
	
	protected JComponent createSettingsPanel() {
		JPanel retVal = new JPanel();
		
		retVal.setLayout(new BorderLayout());
		JScrollPane scriptScroller = new JScrollPane(scriptPanel);
		scriptScroller.getViewport().setBackground(scriptPanel.getBackground());
		retVal.add(scriptScroller, BorderLayout.CENTER);

		if(shouldShowEditor()) {
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
			
			final DropDownButton showEditorBtn = new DropDownButton(act);
			showEditorBtn.setOnlyPopup(true);
			showEditorBtn.setToolTipText("Edit script");
			showEditorBtn.getButtonPopup().addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, (e) -> {
				if(!(Boolean)e.getNewValue()) {
					try {
						getScript().resetContext();
						scriptPanel.updateParams();
					} catch (PhonScriptException e1) {
						Toolkit.getDefaultToolkit().beep();
						LogUtil.severe(e1);
					}
				}
			});
			retVal.add(ButtonBarBuilder.buildOkBar(showEditorBtn), BorderLayout.SOUTH);
		}
		
		return retVal;
	}
	
	private boolean shouldShowEditor() {
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
				LogUtil.severe( e.getLocalizedMessage(), e);
			}
		}
	}

}
