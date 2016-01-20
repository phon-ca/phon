package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public class MoveNodeEdit extends WizardExtensionUndoableEdit {

	private int oldIndex = -1;
	
	private OpNode node = null;
	
	private int newIndex = -1;
	
	public MoveNodeEdit(NodeWizardPanel panel, OpNode node, int newIndex) {
		super(panel);
		
		this.node = node;
		this.newIndex = newIndex;
	}
	
	public void doIt() {
		oldIndex = getWizardExtension().indexOf(node);
		getWizardExtension().removeNode(node);
		getWizardExtension().addNode(newIndex, node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void undo() {
		getWizardExtension().removeNode(node);
		getWizardExtension().addNode(oldIndex, node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}

}
