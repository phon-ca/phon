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

import java.awt.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;
import org.jdesktop.swingx.*;

import com.jgoodies.forms.layout.*;

import ca.phon.plugin.*;
import ca.phon.script.params.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.text.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

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
		param.addPropertyChangeListener(ScriptParam.VISIBLE_PROP, e -> {
			panel.setVisible(param.getVisible());
		});
		panel.setVisible(param.getVisible());
		currentContainer.add(panel);
	}

}
