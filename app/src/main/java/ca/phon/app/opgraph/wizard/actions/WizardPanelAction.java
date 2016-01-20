package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public abstract class WizardPanelAction extends HookableAction {

	private NodeWizardPanel wizardPanel;
	
	public WizardPanelAction(NodeWizardPanel panel) {
		super();
		this.wizardPanel = panel;
	}
	
	public NodeWizardPanel getWizardPanel() {
		return this.wizardPanel;
	}
	
}
