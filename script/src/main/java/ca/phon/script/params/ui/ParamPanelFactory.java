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
package ca.phon.script.params.ui;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.script.params.BooleanScriptParam;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.LabelScriptParam;
import ca.phon.script.params.MultiboolScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.SeparatorScriptParam;
import ca.phon.script.params.StringScriptParam;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Create a script param form.
 */
public class ParamPanelFactory extends VisitorAdapter<ScriptParam> {

	/**
	 * The panel to be returned
	 */
	private JPanel panel;
	
	/**
	 * The current script param container
	 */
	private JPanel currentContainer;
	
	/**
	 * factory
	 * 
	 */
	private ParamComponentFactory factory;
	
	public ParamPanelFactory() {
		super();
		this.panel = new JPanel(new VerticalLayout());
		this.currentContainer = this.panel;
		this.factory = new ParamComponentFactory();
	}
	
	/**
	 * Return the script param form
	 */
	public JPanel getForm() {
		return this.panel;
	}

	@Override
	public void fallbackVisit(ScriptParam obj) {
	}
	
	private JPanel createComponentPanel(JLabel label, JComponent comp) {
		String cols = "20px, fill:pref:grow";
		String rows = "pref, pref";
		FormLayout layout = new FormLayout(cols, rows);
		JPanel compPanel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();
		compPanel.add(label, cc.xyw(1,1,2));
		compPanel.add(comp, cc.xy(2, 2));
		
		return compPanel;
	}
	
	@Visits
	public void visitBooleanScriptParam(BooleanScriptParam param) {
		final JLabel paramLabel = factory.createParamLabel(param);
		final JCheckBox checkBox = factory.createBooleanParamComponent(param);
		final JPanel panel = createComponentPanel(paramLabel, checkBox);
		currentContainer.add(panel);
	}
	
	@Visits
	public void visitEnumScriptParam(EnumScriptParam param) {
		final JLabel paramLabel = factory.createParamLabel(param);
		final JComboBox comboBox = factory.createEnumScriptParamComponent(param);
		final JPanel panel = createComponentPanel(paramLabel, comboBox);
		currentContainer.add(panel);
	}
	
	@Visits
	public void visitLabelScriptParam(LabelScriptParam param) {
		final JLabel paramLabel = factory.createParamLabel(param);
		final JLabel label = factory.createLabelScriptParamComponent(param);
		final JPanel panel = createComponentPanel(paramLabel, label);
		currentContainer.add(panel);
	}
	
	@Visits
	public void visitMultiboolScriptParam(MultiboolScriptParam param) {
		final JLabel paramLabel = factory.createParamLabel(param);
		final JPanel multibool = factory.createMultiBoolScriptParamComponent(param);
		final JPanel panel = createComponentPanel(paramLabel, multibool);
		currentContainer.add(panel);
	}
	
	@Visits
	public void visitSeparatorScriptParam(SeparatorScriptParam param) {
		final JXCollapsiblePane panel = factory.createSeparatorScriptParamComponent(param);
		final JXButton button = factory.createToggleButton(param.getParamDesc(), panel);
		this.panel.add(button);
		this.panel.add(panel);
		this.currentContainer = panel;
	}
	
	@Visits
	public void visitStringScriptParam(StringScriptParam param) {
		final JLabel paramLabel = factory.createParamLabel(param);
		final PromptedTextField textField = factory.createStringScriptParamComponent(param);
		textField.setFont(FontPreferences.getUIIpaFont());
		final JPanel panel = createComponentPanel(paramLabel, textField);
		currentContainer.add(panel);
	}


}
