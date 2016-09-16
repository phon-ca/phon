/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
