package ca.phon.app.opgraph.wizard.edits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class NodeWizardSettingsEdit extends AbstractUndoableEdit {
	
	private OpGraph graph;
	
	private WizardExtension wizardExtension;
	
	private OpNode node;
	
	private boolean addNodeToSettings;
	
	private boolean forced;
	
	public NodeWizardSettingsEdit(OpGraph graph, WizardExtension wizardExt, OpNode node, boolean addNodeToSettings, boolean forced) {
		super();
		
		this.graph = graph;
		this.wizardExtension = wizardExt;
		this.node = node;
		this.addNodeToSettings = addNodeToSettings;
		this.forced = forced;
		
		if(addNodeToSettings) {
			wizardExt.addNode(node);
			wizardExt.setNodeForced(node, forced);
		} else {
			wizardExt.removeNode(node);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		if(addNodeToSettings) {
			wizardExtension.removeNode(node);
		} else {
			wizardExtension.addNode(node);
			wizardExtension.setNodeForced(node, forced);
		}
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		
		if(addNodeToSettings) {
			wizardExtension.addNode(node);
			wizardExtension.setNodeForced(node, forced);
		} else {
			wizardExtension.removeNode(node);
		}
	}

	@Override
	public boolean canRedo() {
		return true;
	}
	
}
