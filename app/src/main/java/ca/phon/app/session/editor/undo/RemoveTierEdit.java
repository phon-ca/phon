/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;

public class RemoveTierEdit extends AddTierEdit {

	private static final long serialVersionUID = 5829729907299422281L;

	public RemoveTierEdit(SessionEditor editor, TierDescription tierDesc,
			TierViewItem tvi) {
		super(editor, tierDesc, tvi);
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo delete tier " + tierDescription.getName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo delete tier " + tierDescription.getName();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.doIt();
	}

	@Override
	public void doIt() {
		super.undo();
	}
	
}
