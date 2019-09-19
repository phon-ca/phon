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
package ca.phon.script.params.ui;

import java.awt.BorderLayout;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.plugin.PluginManager;
import ca.phon.script.params.BooleanScriptParam;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.LabelScriptParam;
import ca.phon.script.params.MultiboolScriptParam;
import ca.phon.script.params.PatternScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.SeparatorScriptParam;
import ca.phon.script.params.StringScriptParam;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Create a script param form.
 */
public class ParamPanelFactory extends VisitorAdapter<ScriptParam> {

	/**
	 * The panel to be returned
	 */
	private JPanel rootPanel;
	
	/**
	 * The current script param container
	 */
	private JPanel currentContainer;
	
	/**
	 * factory
	 * 
	 */
	private ParamComponentFactory factory;
	
	private final Supplier<JPanel> panelSupplier;
	
	public ParamPanelFactory() {
		this(JPanel::new);
	}
	
	public ParamPanelFactory(Supplier<JPanel> supplier) {
		this(supplier.get(), supplier);
	}
	
	public ParamPanelFactory(JPanel rootPanel, Supplier<JPanel> panelSupplier) {
		super();
		this.rootPanel = rootPanel;
		this.rootPanel.setLayout(new VerticalLayout());
		this.panelSupplier = panelSupplier;
		this.currentContainer = this.rootPanel;
		this.factory = new ParamComponentFactory();
	}
	
	/**
	 * Return the script param form
	 */
	public JPanel getForm() {
		return this.rootPanel;
	}

	@Override
	public void fallbackVisit(ScriptParam param) {
		Optional<ScriptParamComponentFactory> factoryOpt = 
				PluginManager.getInstance().getExtensionPoints(ScriptParamComponentFactory.class)
					.stream().map( (extPt) -> extPt.getFactory().createObject() )
					.filter( (compFactory) -> compFactory.canCreateScriptParamComponent(param) )
					.findAny();
		
		if(factoryOpt.isPresent()) {
			var compFactory = factoryOpt.get();
			final JLabel paramLabel = factory.createParamLabel(param);
			final JComponent paramComp = compFactory.createScriptParamComponent(param);
			final JPanel panel = createComponentPanel(paramLabel, paramComp);
			currentContainer.add(panel);
		}
	}
	
	private JPanel createComponentPanel(JLabel label, JComponent comp) {
		String cols = "20px, fill:pref:grow";
		String rows = "pref, pref";
		FormLayout layout = new FormLayout(cols, rows);
		JPanel compPanel = this.panelSupplier.get();
		compPanel.setLayout(layout);
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
		final JComponent comp = factory.createEnumScriptParamComponent(param);
		final JPanel panel = createComponentPanel(paramLabel, comp);
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
		final JXButton button = factory.createToggleButton(param.getParamDesc(), panel, param);
		this.rootPanel.add(button);
		this.rootPanel.add(panel);
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
	
	@Visits
	public void visitPatternScriptParam(PatternScriptParam param) {
		final JLabel paramLabel = factory.createParamLabel(param);
		final RSyntaxTextArea textArea = factory.createPatternScriptParamComponent(param);
		final RTextScrollPane scroller = new RTextScrollPane(textArea);
		scroller.setLineNumbersEnabled(true);
		ErrorStrip strip = new ErrorStrip(textArea);
		strip.setOpaque(false);
		
		final JComponent container = currentContainer;
		param.addPropertyChangeListener( PatternScriptParam.VISIBLE_ROWS_PROP, (e) -> {
			textArea.setRows(param.getVisibleRows());
			scroller.revalidate();
			container.revalidate();
		});
		
		final JPanel p = panelSupplier.get();
		p.setLayout(new BorderLayout());
		p.add(scroller, BorderLayout.CENTER);
		p.add(strip, BorderLayout.LINE_END);
		
		final JPanel panel = createComponentPanel(paramLabel, p);
		currentContainer.add(panel);
	}

}
