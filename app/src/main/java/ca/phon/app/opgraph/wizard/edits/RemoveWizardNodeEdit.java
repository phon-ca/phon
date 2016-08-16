package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public class RemoveWizardNodeEdit extends WizardExtensionUndoableEdit {

	private static final long serialVersionUID = -2662867852084560083L;

	private OpNode node;
	
	private int index = -1;
	
	public RemoveWizardNodeEdit(NodeWizardPanel wizardPanel, OpNode node) {
		super(wizardPanel);
		
		this.node = node;
	}
	
	public void doIt() {
		index = getWizardExtension().indexOf(node);
		getWizardExtension().removeNode(node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void undo() {
		getWizardExtension().addNode(index, node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}

}
