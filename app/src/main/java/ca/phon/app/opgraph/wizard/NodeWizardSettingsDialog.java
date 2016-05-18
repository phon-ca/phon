package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import ca.gedge.opgraph.OpNode;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

public class NodeWizardSettingsDialog extends JDialog {

	private final WizardExtension wizardExtension;
	
	private NodeWizardSettingsPanel settingsPanel;
	
	private JButton okButton;
	
	private JButton closeButton;
	
	private boolean wasCanceled = true;
	
	public NodeWizardSettingsDialog(WizardExtension wizardExtension) {
		super();
		
		this.wizardExtension = wizardExtension;
		
		setTitle("Wizard Settings");
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final DialogHeader header = new DialogHeader("Wizard Settings", 
				"Setup title and information messages for wizard steps");
		add(header, BorderLayout.NORTH);
		
		settingsPanel = new NodeWizardSettingsPanel(wizardExtension);
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
	
	public NodeWizardSettingsPanel getSettings() {
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
		setVisible(true);
		
		return !wasCanceled;
	}
	
}
