package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class ChangeWizardInfoEdit extends WizardExtensionUndoableEdit {
	
	private static final long serialVersionUID = -5317622356377461585L;

	private OpNode node;

	private String oldTitle;
	
	private String oldMessage;
	
	private String title;
	
	private String message;
	
	public ChangeWizardInfoEdit(NodeWizardPanel wizardPanel, String title, String message,
			String oldTitle, String oldMessage) {
		this(wizardPanel, null, title, message, oldTitle, oldMessage);
	}

	public ChangeWizardInfoEdit(NodeWizardPanel wizardPanel, OpNode node, String title, String message,
			String oldTitle, String oldMessage) {
		super(wizardPanel);
		
		this.node = node;
		this.title = title;
		this.message = message;
		this.oldTitle = oldTitle;
		this.oldMessage = oldMessage;
	}
	
	public void doIt() {
		if(node == null) {
			getWizardExtension().setWizardTitle(this.title);
			getWizardExtension().setWizardMessage(this.message);
		} else {
			getWizardExtension().setNodeTitle(node, this.title);
			getWizardExtension().setNodeMessage(node, this.message);
		}
		getWizardPanel().updateTable();
	}
	
	@Override
	public void undo() {
		if(node == null) {
			getWizardExtension().setWizardTitle(this.oldTitle);
			getWizardExtension().setWizardMessage(this.oldMessage);
		} else {
			getWizardExtension().setNodeTitle(node, this.oldTitle);
			getWizardExtension().setNodeMessage(node, this.oldMessage);
		}
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}
	
}
