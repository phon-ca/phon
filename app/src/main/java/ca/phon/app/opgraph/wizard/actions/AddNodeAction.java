package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.edits.AddWizardNodeEdit;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Add the selected node in the current graph document
 * to the NodeWizard
 */
public class AddNodeAction extends WizardPanelAction {

	private static final long serialVersionUID = -3777497407512293301L;

	public AddNodeAction(NodeWizardPanel panel) {
		super(panel);
		
		final ImageIcon addIcon = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		putValue(NAME, "Add node");
		putValue(SHORT_DESCRIPTION, "Add selected node to wizard");
		putValue(SMALL_ICON, addIcon);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final NodeWizardPanel panel = getWizardPanel();
		final OpNode selectedNode =
				panel.getDocument().getSelectionModel().getSelectedNode();
		if(selectedNode != null) {
			final AddWizardNodeEdit edit = 
					new AddWizardNodeEdit(panel, selectedNode);
			edit.doIt();
			panel.getDocument().getUndoSupport().postEdit(edit);
		}
	}

}
