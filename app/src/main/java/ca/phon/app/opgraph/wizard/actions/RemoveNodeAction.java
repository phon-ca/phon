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

import javax.swing.undo.CompoundEdit;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.edits.RemoveOptionalNodeEdit;
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
			if(panel.getVisibleTab().equals("Wizard Nodes")) {
				final RemoveWizardNodeEdit rmEdit = new RemoveWizardNodeEdit(panel, node);
				rmEdit.doIt();
				edit.addEdit(rmEdit);
			} else {
				final RemoveOptionalNodeEdit rmEdit = new RemoveOptionalNodeEdit(panel, node);
				rmEdit.doIt();
				edit.addEdit(rmEdit);
			}
		}
		edit.end();
		
		if(toRemove.size() > 0)
			panel.getDocument().getUndoSupport().postEdit(edit);
	}

}
