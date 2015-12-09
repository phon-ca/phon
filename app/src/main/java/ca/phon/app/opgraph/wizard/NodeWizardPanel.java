package ca.phon.app.opgraph.wizard;

import javax.swing.JButton;
import javax.swing.JList;

import ca.phon.opgraph.editor.OpgraphEditor;

/**
 * Provides a UI for selecting and ordering nodes in the wizard.
 *
 */
public class NodeWizardPanel {
	
	private WizardExtension wizardExtension;
	
	private OpgraphEditor editor;
	
	private JList<String> nodeIdList;
	
	private JButton addToWizardButton;
	
	private JButton removeFromWizardButton;
	
	private JButton moveUpButton;
	
	private JButton moveDownButton;
	
	public NodeWizardPanel(OpgraphEditor editor, WizardExtension extension) {
		super();
		this.wizardExtension = extension;
		this.editor = editor;
	
		init();
	}
	
	private void init() {
		
	}

}
