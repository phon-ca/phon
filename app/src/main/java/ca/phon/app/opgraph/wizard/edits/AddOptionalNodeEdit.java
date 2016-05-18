package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public class AddOptionalNodeEdit extends WizardExtensionUndoableEdit {

	private static final long serialVersionUID = -7784949341929184890L;

	private OpNode node;

	public AddOptionalNodeEdit(NodeWizardPanel wizardPanel, OpNode node) {
		super(wizardPanel);
		
		this.node = node;
	}
	
	public void doIt() {
		if(!getWizardExtension().getOptionalNodes().contains(node)) {
			getWizardExtension().addOptionalNode(node);
			getWizardPanel().updateOptionalTable();
		}
	}
	
	@Override
	public void undo() {
		getWizardExtension().removeOptionalNode(node);
		getWizardPanel().updateOptionalTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}
}
