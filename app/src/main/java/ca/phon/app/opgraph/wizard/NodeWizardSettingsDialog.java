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
package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import ca.gedge.opgraph.OpGraph;
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
