/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.*;
import ca.phon.session.*;

public class RemoveTierAction extends TierManagementAction {

	private static final long serialVersionUID = 2530259863724783877L;

	private TierDescription td;
	
	private TierViewItem tvi;
	
	public RemoveTierAction(SessionEditor editor, TierOrderingEditorView view,
			TierDescription td, TierViewItem tvi) {
		super(editor, view);
		this.td = td;
		this.tvi = tvi;
		
		putValue(NAME, "Delete tier " + td.getName());
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final RemoveTierEdit edit = new RemoveTierEdit(getEditor(), td, tvi);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
