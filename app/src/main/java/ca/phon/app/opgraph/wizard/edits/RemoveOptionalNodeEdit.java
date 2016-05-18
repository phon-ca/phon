package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public class RemoveOptionalNodeEdit extends WizardExtensionUndoableEdit {

	private static final long serialVersionUID = -2662867852084560083L;

	private OpNode node;
	
	private int index = -1;
	
	public RemoveOptionalNodeEdit(NodeWizardPanel wizardPanel, OpNode node) {
		super(wizardPanel);
		
		this.node = node;
	}
	
	public void doIt() {
		index = getWizardExtension().getOptionalNodes().indexOf(node);
		getWizardExtension().removeOptionalNode(node);
		getWizardPanel().updateOptionalTable();
	}
	
	@Override
	public void undo() {
		getWizardExtension().addOptionalNode(index, node);
		getWizardPanel().updateOptionalTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}
	
}
