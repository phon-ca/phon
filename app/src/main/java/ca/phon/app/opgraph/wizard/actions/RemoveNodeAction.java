package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.undo.CompoundEdit;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.edits.RemoveWizardNodeEdit;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class RemoveNodeAction extends WizardPanelAction {

	private static final long serialVersionUID = -4809108679623649915L;

	public RemoveNodeAction(NodeWizardPanel panel) {
		super(panel);
		
		putValue(NAME, "Remove from wizard");
		putValue(SHORT_DESCRIPTION, "Remove selected steps from wizard");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final NodeWizardPanel panel = getWizardPanel();
		final List<OpNode> toRemove = panel.getSelectedNodes();
		
		final CompoundEdit edit = new CompoundEdit();
		for(OpNode node:toRemove) {
			final RemoveWizardNodeEdit rmEdit = new RemoveWizardNodeEdit(panel, node);
			rmEdit.doIt();
			edit.addEdit(rmEdit);
		}
		edit.end();
		
		if(toRemove.size() > 0)
			panel.getDocument().getUndoSupport().postEdit(edit);
	}

}
