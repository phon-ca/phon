package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.NodeWizardSettingsDialog;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.WizardExtensionUndoableEdit;

public class WizardSettingsAction extends HookableAction {

	private static final long serialVersionUID = 5573839326143897285L;

	private final static String TXT = "Wizard Settings...";
	private final static String DESC = "Modify settings for the wizard";
	
	private final OpgraphEditor editor;
	
	public WizardSettingsAction(OpgraphEditor editor) {
		super();
		
		this.editor = editor;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpGraph graph = editor.getModel().getDocument().getGraph();
		final WizardExtension ext = graph.getExtension(WizardExtension.class);
		if(ext == null) return;
		
		final NodeWizardSettingsDialog dialog = new NodeWizardSettingsDialog(graph, ext);
		if(dialog.showDialog()) {
			final WizardExtensionUndoableEdit edit = new WizardExtensionUndoableEdit(graph, ext, dialog.getSettings().getUpdatedWizardExtension());
			editor.getModel().getDocument().getUndoSupport().postEdit(edit);
		}
	}

}
