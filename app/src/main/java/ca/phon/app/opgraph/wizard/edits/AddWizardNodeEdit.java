package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public class AddWizardNodeEdit extends WizardExtensionUndoableEdit {
	
	private static final long serialVersionUID = -7784949341929184890L;

	private OpNode node;

	public AddWizardNodeEdit(NodeWizardPanel wizardPanel, OpNode node) {
		super(wizardPanel);
		
		this.node = node;
	}
	
	public void doIt() {
		if(!getWizardExtension().containsNode(node)) {
			getWizardExtension().addNode(node);
			getWizardPanel().updateTable();
		}
	}
	
	@Override
	public void undo() {
		getWizardExtension().removeNode(node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}

}
