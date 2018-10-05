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
