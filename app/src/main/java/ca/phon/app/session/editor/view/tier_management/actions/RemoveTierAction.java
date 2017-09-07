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
package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RemoveTierEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
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
