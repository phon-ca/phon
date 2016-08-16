package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.undo.CompoundEdit;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.edits.MoveNodeEdit;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Move selected nodes up/down in the list of wizard steps.
 *
 */
public class MoveNodeAction extends WizardPanelAction {

	public static final int UP = SwingConstants.NORTH;
	
	public static final int DOWN = SwingConstants.SOUTH;
	
	private static final long serialVersionUID = 9118395603713827836L;

	private int direction;
	
	public MoveNodeAction(NodeWizardPanel panel, int direction) {
		super(panel);
		
		this.direction = direction;
		
		String name = "Move " + 
				(this.direction == UP ? "up" : "down");
		String msg = "Move selected node " + 
				(this.direction == UP ? "up" : "down");
		String icn = 
				(this.direction == UP ? "actions/go-up" : "actions/go-down");
		putValue(NAME, name);
		putValue(SHORT_DESCRIPTION, msg);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(icn, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final List<OpNode> selectedNodes = getWizardPanel().getSelectedNodes();
		final CompoundEdit edit = new CompoundEdit();
		final NodeWizardPanel panel = getWizardPanel();
		
		for(OpNode node:selectedNodes) {
			int nodeIdx = panel.getWizardExtension().indexOf(node);
			int newIdx =
					(this.direction == UP ? Math.max(0, nodeIdx-1) : Math.min(panel.getWizardExtension().size(), nodeIdx+1));
			final MoveNodeEdit moveEdit = new MoveNodeEdit(panel, node, newIdx);
			moveEdit.doIt();
			edit.addEdit(moveEdit);
		}
		
		edit.end();
		if(selectedNodes.size() > 0) {
			panel.getDocument().getUndoSupport().postEdit(edit);
		}
	}

}
