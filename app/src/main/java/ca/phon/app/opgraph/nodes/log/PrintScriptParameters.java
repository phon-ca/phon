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
package ca.phon.app.opgraph.nodes.log;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.script.*;
import ca.phon.script.params.*;

/**
 *
 */
@OpNodeInfo(
		name="Format Script Parameters",
		description="Format script parameters for report output",
		category="Report",
		showInLibrary=true)
public class PrintScriptParameters extends OpNode implements NodeSettings {

	private final InputField scriptInputField =
			new InputField("script", "Script", false, true, PhonScript.class);

	private final OutputField stringOutputField =
			new OutputField("text", "", true, String.class);

	private boolean printOnlyChanged = true;

	/**
	 * List of paramIds/categories to always include
	 * categories are prefixed with a '@' symbol
	 */
	private List<String> includes = new ArrayList<>();

	/**
	 * List of paramIds/categories to always exclude
	 * categories are prefixed with a '@' symbol
	 */
	private List<String> excludes = new ArrayList<>();

	private JPanel settingsPanel;
	private JCheckBox printOnlyChangedBox;
	private RSyntaxTextArea includesArea;
	private RSyntaxTextArea excludesArea;

	public PrintScriptParameters() {
		super();

		putField(scriptInputField);
		putField(stringOutputField);

		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final PhonScript script = (PhonScript)context.get(scriptInputField);

		StringBuffer buffer = new StringBuffer();
		try {
			final ScriptParameters params = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
			buffer.append(params.toHTMLString(isPrintOnlyChanged(), getIncludes(), getExcludes()));
			buffer.append("\n");
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e.getLocalizedMessage());
		}

		context.put(stringOutputField, buffer.toString());
	}

	public boolean isPrintOnlyChanged() {
		return (printOnlyChangedBox != null ? printOnlyChangedBox.isSelected() : printOnlyChanged);
	}

	public void setPrintOnlyChagned(boolean printOnlyChanged) {
		this.printOnlyChanged = printOnlyChanged;
		if(printOnlyChangedBox != null)
			printOnlyChangedBox.setSelected(printOnlyChanged);
	}

	public List<String> getIncludes() {
		if(this.includesArea != null) {
			return Arrays.stream(this.includesArea.getText().split("\n"))
						.filter( (s) -> s.trim().length() > 0 ).collect(Collectors.toList());
		} else {
			return this.includes;
		}
	}

	public void setIncludes(List<String> includes) {
		this.includes.clear();
		this.includes.addAll(includes);

		if(this.includesArea != null) {
			String txt = includes.stream().collect(Collectors.joining("\n"));
			this.includesArea.setText(txt);
		}
	}

	public List<String> getExcludes() {
		if(this.excludesArea != null) {
			return Arrays.stream(this.excludesArea.getText().split("\n"))
					.filter( (s) -> s.trim().length() > 0 ).collect(Collectors.toList());
		} else {
			return this.excludes;
		}
	}

	public void setExcludes(List<String> excludes) {
		this.excludes.clear();
		this.excludes.addAll(excludes);

		if(this.excludesArea != null) {
			String txt = excludes.stream().collect(Collectors.joining("\n"));
			this.excludesArea.setText(txt);
		}
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new GridBagLayout());
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;

			printOnlyChangedBox = new JCheckBox("Print only changed values");
			printOnlyChangedBox.setSelected(printOnlyChanged);
			settingsPanel.add(printOnlyChangedBox, gbc);

			++gbc.gridy;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1.0;
			includesArea = new RSyntaxTextArea();
			includesArea.setText(includes.stream().collect(Collectors.joining("\n")));
			final RTextScrollPane includesScroller = new RTextScrollPane(includesArea);
			includesScroller.setBorder(BorderFactory.createTitledBorder("Includes"));
			settingsPanel.add(includesScroller, gbc);

			++gbc.gridy;
			excludesArea = new RSyntaxTextArea();
			excludesArea.setText(excludes.stream().collect(Collectors.joining("\n")));
			final RTextScrollPane excludesScroller = new RTextScrollPane(excludesArea);
			excludesScroller.setBorder(BorderFactory.createTitledBorder("Excludes"));
			settingsPanel.add(excludesScroller, gbc);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		final Properties retVal = new Properties();

		retVal.put("printOnlyChanged", Boolean.toString(isPrintOnlyChanged()));
		String includes = getIncludes().stream().collect(Collectors.joining("\n"));
		retVal.put("includes", includes);
		String excludes = getExcludes().stream().collect(Collectors.joining("\n"));
		retVal.put("excludes", excludes);

		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		if(properties.containsKey("printOnlyChanged")) {
			setPrintOnlyChagned(Boolean.parseBoolean(properties.getProperty("printOnlyChanged")));
		}

		if(properties.containsKey("includes")) {
			List<String> includes =
					Arrays.stream(properties.getProperty("includes").split("\n")).collect(Collectors.toList());
			setIncludes(includes);
		}

		if(properties.containsKey("excludes")) {
			List<String> excludes =
					Arrays.stream(properties.getProperty("excludes").split("\n")).collect(Collectors.toList());
			setExcludes(excludes);
		}
	}

}
