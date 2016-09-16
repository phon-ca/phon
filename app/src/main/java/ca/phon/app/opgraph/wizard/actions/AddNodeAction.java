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

import javax.swing.ImageIcon;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.edits.AddOptionalNodeEdit;
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
			if(panel.getVisibleTab().equals("Wizard Nodes")) {
				final AddWizardNodeEdit edit = 
						new AddWizardNodeEdit(panel, selectedNode);
				edit.doIt();
				panel.getDocument().getUndoSupport().postEdit(edit);
			} else {
				final AddOptionalNodeEdit edit = 
						new AddOptionalNodeEdit(panel, selectedNode);
				edit.doIt();
				panel.getDocument().getUndoSupport().postEdit(edit);
			}
		}
	}

}
