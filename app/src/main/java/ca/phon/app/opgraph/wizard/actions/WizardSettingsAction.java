/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
		final OpGraph graph = editor.getModel().getDocument().getRootGraph();
		final WizardExtension ext = graph.getExtension(WizardExtension.class);
		if(ext == null) return;
		
		final NodeWizardSettingsDialog dialog = new NodeWizardSettingsDialog(graph, ext);
		if(dialog.showDialog()) {
			final WizardExtensionUndoableEdit edit = new WizardExtensionUndoableEdit(graph, ext, dialog.getSettings().getUpdatedWizardExtension());
			editor.getModel().getDocument().getUndoSupport().postEdit(edit);
		}
	}

}
