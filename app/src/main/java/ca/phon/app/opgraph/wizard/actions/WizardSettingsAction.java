/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.wizard.NodeWizardSettingsDialog;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.WizardExtensionUndoableEdit;
import ca.phon.opgraph.OpGraph;

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
