package ca.phon.app.opgraph.wizard.edits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class WizardExtensionUndoableEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;

	private final NodeWizardPanel wizardPanel;
	
	public WizardExtensionUndoableEdit(NodeWizardPanel panel) {
		super();
		this.wizardPanel = panel;
	}
	
	public NodeWizardPanel getWizardPanel() {
		return this.wizardPanel;
	}
	
	public WizardExtension getWizardExtension() {
		return getWizardPanel().getWizardExtension();
	}
	
}
