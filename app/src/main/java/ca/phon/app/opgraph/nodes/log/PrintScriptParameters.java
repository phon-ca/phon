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
package ca.phon.app.opgraph.nodes.log;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.BooleanScriptParam;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.MultiboolScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.params.SeparatorScriptParam;
import ca.phon.script.params.StringScriptParam;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

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

		final ScriptParamVisitor visitor = new ScriptParamVisitor();
		try {
			final ScriptParameters params = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
			params.forEach( (p) -> visitor.visit(p) );
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e.getLocalizedMessage());
		}

		context.put(stringOutputField, visitor.buffer.toString());
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

	public class ScriptParamVisitor extends VisitorAdapter<ScriptParam> {

		StringBuffer buffer;

		String currentCategory = "General";

		boolean printCategoryHeader = true;

		public ScriptParamVisitor() {
			super();
			buffer = new StringBuffer();
		}

		@Override
		public void fallbackVisit(ScriptParam obj) {
		}

		private void printCategoryHeader() {
			if(printCategoryHeader) {
				buffer.append("\n\n#h3(\"").append(currentCategory).append("\")").append('\n');
				printCategoryHeader = false;
			}
		}

		private void printKey(String key) {
			// remove ':' from end of key as it will be added after
			if(key.endsWith(":")) {
				key = key.substring(0, key.length()-1);
			}
			if(key.trim().length() > 0) {
				buffer.append(key).append(": ");
			}
		}

		private void printValue(Object value) {
			if(value == null || value.toString().length() == 0) return;
			buffer.append("__").append(value).append("__");
		}

		private boolean checkIncludeParam(ScriptParam param) {
			if(isPrintOnlyChanged()) {
				boolean isForceInclude =
						getIncludes().contains("@" + currentCategory) || getIncludes().contains(param.getParamId());
				boolean isExcluded =
						getExcludes().contains("@" + currentCategory) || getExcludes().contains(param.getParamId());

				if(isForceInclude) {
					return !isExcluded;
				} else {
					return !isExcluded && param.hasChanged();
				}
			} else {
				if(getExcludes().contains("@" + currentCategory) ||
						getExcludes().contains(param.getParamId()))
					return false;
				else
					return true;
			}
		}

		@Visits
		public void visitSeparator(SeparatorScriptParam param) {
			currentCategory = param.getParamDesc();
			printCategoryHeader = true;
		}

		@Visits
		public void visitBooleanParam(BooleanScriptParam param) {
			if(checkIncludeParam(param)) {
				printCategoryHeader();

				String name =
						(param.getParamDesc().trim().length() > 0
								? param.getParamDesc()
								: param.getLabelText());
				buffer.append("\n * ");
				printKey(name);
				if((Boolean)param.getValue(param.getParamId())) {
					printValue("yes");
				} else {
					printValue("no");
				}
			}
		}

		@Visits
		public void visitMultiBoolParam(MultiboolScriptParam param) {
			if(checkIncludeParam(param)) {
				printCategoryHeader();

				if(param.getParamDesc().trim().length() > 0) {
					buffer.append("\n * ");
					printKey(param.getParamDesc());
				}
				for(int i = 0; i < param.getNumberOfOptions(); i++) {
					buffer.append("\n   * ");
					printKey(param.getOptionText(i));

					if((Boolean)param.getValue(param.getOptionId(i))) {
						printValue("yes");
					} else {
						printValue("no");
					}
				}
			}
		}

		@Visits
		public void visitStringParam(StringScriptParam param) {
			if(checkIncludeParam(param)) {
				printCategoryHeader();

				buffer.append("\n * ");
				printKey(param.getParamDesc());
				printValue(param.getValue(param.getParamId()));
			}
		}

		@Visits
		public void visitEnumParam(EnumScriptParam param) {
			if(checkIncludeParam(param)) {
				printCategoryHeader();

				buffer.append("\n * ");
				printKey(param.getParamDesc());
				printValue(((EnumScriptParam.ReturnValue)param.getValue(param.getParamId())));
			}
		}

	}

}
