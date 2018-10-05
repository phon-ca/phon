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
package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import ca.phon.opgraph.OpGraph;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

public class NodeWizardSettingsDialog extends JDialog {

	private final OpGraph graph;
	
	private final WizardExtension wizardExtension;
	
	private WizardSettingsPanel settingsPanel;
	
	private JButton okButton;
	
	private JButton closeButton;
	
	private boolean wasCanceled = true;
	
	public NodeWizardSettingsDialog(OpGraph graph, WizardExtension wizardExtension) {
		super();
		
		this.graph = graph;
		this.wizardExtension = wizardExtension;
		
		setTitle("Wizard Settings");
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Wizard Settings", 
				"Setup title and information messages for wizard steps");
		add(header, BorderLayout.NORTH);
		
		settingsPanel = new WizardSettingsPanel(graph, wizardExtension);
		add(settingsPanel, BorderLayout.CENTER);
		
		final PhonUIAction okAct = new PhonUIAction(this, "onOk");
		okAct.putValue(PhonUIAction.NAME, "Ok");
		okButton = new JButton(okAct);
		
		final PhonUIAction closeAct = new PhonUIAction(this, "onCancel");
		closeAct.putValue(PhonUIAction.NAME, "Cancel");
		closeButton = new JButton(closeAct);
		
		getRootPane().setDefaultButton(okButton);
		
		final JComponent btnPanel = 
				ButtonBarBuilder.buildOkCancelBar(okButton, closeButton);
		add(btnPanel, BorderLayout.SOUTH);
	}
	
	public WizardSettingsPanel getSettings() {
		return this.settingsPanel;
	}
	
	public void onOk() {
		wasCanceled = false;
		
		onCancel();
	}
	
	public void onCancel() {
		setVisible(false);
		dispose();
	}
	
	/**
	 * Display modal dialog.
	 * 
	 * @return <code>true</code> if Ok was pressed, <code>false</code> otherwise
	 * @return
	 */
	public boolean showDialog() {
		setModal(true);
		pack();
		setSize(1024, 768);
		CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
		if(cmf != null) {
			setLocationRelativeTo(cmf);
		}
		setVisible(true);
		
		return !wasCanceled;
	}
	
}
