package ca.phon.app.opgraph.wizard.edits;

import javax.swing.undo.AbstractUndoableEdit;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class NodeWizardOptionalsEdit extends AbstractUndoableEdit {

	private OpGraph graph;
	
	private WizardExtension wizardExtension;
	
	private OpNode node;
	
	private boolean addNodeToOptionals;
	
	private boolean defaultValue;
	
	public NodeWizardOptionalsEdit(OpGraph graph, WizardExtension wizardExt, OpNode node, boolean addNodeToOptionals, boolean defaultValue) {
		super();
		
		this.graph = graph;
		this.wizardExtension = wizardExt;
		this.node = node;
		this.addNodeToOptionals = addNodeToOptionals;
		this.defaultValue = defaultValue;
		
		if(addNodeToOptionals) {
			wizardExt.addOptionalNode(node);
			wizardExt.setOptionalNodeDefault(node, defaultValue);
		} else {
			wizardExt.removeOptionalNode(node);
		}
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void undo() {
		super.undo();
		
		if(addNodeToOptionals) {
			wizardExtension.removeOptionalNode(node);
		} else {
			wizardExtension.addOptionalNode(node);
			wizardExtension.setOptionalNodeDefault(node, defaultValue);
		}
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}
	
	@Override
	public void redo() {
		super.redo();
		
		if(addNodeToOptionals) {
			wizardExtension.addOptionalNode(node);
			wizardExtension.setOptionalNodeDefault(node, defaultValue);
		} else {
			wizardExtension.removeOptionalNode(node);
		}
	}
	
}
