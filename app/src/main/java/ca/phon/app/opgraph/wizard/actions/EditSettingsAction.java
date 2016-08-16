package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.NodeWizardSettingsDialog;
import ca.phon.app.opgraph.wizard.NodeWizardSettingsPanel;
import ca.phon.app.opgraph.wizard.edits.ChangeWizardInfoEdit;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class EditSettingsAction extends WizardPanelAction {

	private static final long serialVersionUID = 7323296771808991518L;

	public EditSettingsAction(NodeWizardPanel panel) {
		super(panel);
		
		putValue(NAME, "Wizard settings");
		putValue(SHORT_DESCRIPTION, "Edit wizard information messages");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon("categories/preferences", IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final NodeWizardPanel panel = getWizardPanel();
		final NodeWizardSettingsDialog dialog = new NodeWizardSettingsDialog(panel.getWizardExtension());
		
		if(dialog.showDialog()) {
			final CompoundEdit edit = new CompoundEdit();
			
			String newTitle = 
					dialog.getSettings().getTitle(NodeWizardSettingsPanel.WIZARD_INFO);
			String newMessage =
					dialog.getSettings().getMessage(NodeWizardSettingsPanel.WIZARD_INFO);
			ChangeWizardInfoEdit infoEdit = new ChangeWizardInfoEdit(panel, newTitle, newMessage,
					panel.getWizardExtension().getWizardTitle(), panel.getWizardExtension().getWizardMessage());
			infoEdit.doIt();
			edit.addEdit(infoEdit);
			
			for(OpNode node:panel.getWizardExtension()) {
				newTitle = 
					dialog.getSettings().getTitle(node.getId());
				newMessage =
					dialog.getSettings().getMessage(node.getId());
				String oldTitle = 
					panel.getWizardExtension().getNodeTitle(node);
				String oldMessage =
					panel.getWizardExtension().getNodeMessage(node);
				
				ChangeWizardInfoEdit nodeEdit = new ChangeWizardInfoEdit(panel, node,
						newTitle, newMessage, oldTitle, oldMessage);
				nodeEdit.doIt();
				edit.addEdit(nodeEdit);
			}
			
			edit.end();
			panel.getDocument().getUndoSupport().postEdit(edit);
		}
	}

}
