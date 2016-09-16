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

import javax.swing.undo.CompoundEdit;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;
import ca.phon.app.opgraph.wizard.NodeWizardSettingsDialog;
import ca.phon.app.opgraph.wizard.NodeWizardSettingsPanel;
import ca.phon.app.opgraph.wizard.WizardInfoMessageFormat;
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
			WizardInfoMessageFormat newFormat =
					dialog.getSettings().getFormat(NodeWizardSettingsPanel.WIZARD_INFO);
			ChangeWizardInfoEdit infoEdit = new ChangeWizardInfoEdit(panel, newTitle, newMessage, newFormat,
					panel.getWizardExtension().getWizardTitle(), panel.getWizardExtension().getWizardMessage(), panel.getWizardExtension().getWizardMessageFormat());
			infoEdit.doIt();
			edit.addEdit(infoEdit);
			
			for(OpNode node:panel.getWizardExtension()) {
				newTitle = 
					dialog.getSettings().getTitle(node.getId());
				newMessage =
					dialog.getSettings().getMessage(node.getId());
				newFormat =
					dialog.getSettings().getFormat(node.getId());
				String oldTitle = 
					panel.getWizardExtension().getNodeTitle(node);
				String oldMessage =
					panel.getWizardExtension().getNodeMessage(node);
				WizardInfoMessageFormat oldFormat =
					panel.getWizardExtension().getNodeMessageFormat(node);
				
				ChangeWizardInfoEdit nodeEdit = new ChangeWizardInfoEdit(panel, node,
						newTitle, newMessage, newFormat, oldTitle, oldMessage, oldFormat);
				nodeEdit.doIt();
				edit.addEdit(nodeEdit);
			}
			
			edit.end();
			panel.getDocument().getUndoSupport().postEdit(edit);
		}
	}

}
