package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.WizardInfoMessageFormat;

public class ChangeWizardInfoEdit extends WizardExtensionUndoableEdit {
	
	private static final long serialVersionUID = -5317622356377461585L;

	private OpNode node;

	private String oldTitle;
	
	private String oldMessage;
	
	private WizardInfoMessageFormat oldFormat;
	
	private String title;
	
	private String message;
	
	private WizardInfoMessageFormat format;
	
	public ChangeWizardInfoEdit(NodeWizardPanel wizardPanel, String title, String message, WizardInfoMessageFormat format,
			String oldTitle, String oldMessage, WizardInfoMessageFormat oldFormat) {
		this(wizardPanel, null, title, message, format, oldTitle, oldMessage, oldFormat);
	}

	public ChangeWizardInfoEdit(NodeWizardPanel wizardPanel, OpNode node, String title, String message, WizardInfoMessageFormat format,
			String oldTitle, String oldMessage, WizardInfoMessageFormat oldFormat) {
		super(wizardPanel);
		
		this.node = node;
		this.title = title;
		this.format = format;
		this.message = message;
		this.oldTitle = oldTitle;
		this.oldMessage = oldMessage;
		this.oldFormat = oldFormat;
	}
	
	public void doIt() {
		if(node == null) {
			getWizardExtension().setWizardTitle(this.title);
			getWizardExtension().setWizardMessage(this.message, this.format);
		} else {
			getWizardExtension().setNodeTitle(node, this.title);
			getWizardExtension().setNodeMessage(node, this.message, this.format);
		}
		getWizardPanel().updateTable();
	}
	
	@Override
	public void undo() {
		if(node == null) {
			getWizardExtension().setWizardTitle(this.oldTitle);
			getWizardExtension().setWizardMessage(this.oldMessage, this.oldFormat);
		} else {
			getWizardExtension().setNodeTitle(node, this.oldTitle);
			getWizardExtension().setNodeMessage(node, this.oldMessage, this.oldFormat);
		}
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}
	
}
