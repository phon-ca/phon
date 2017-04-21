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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;

public class SessionLanguageEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 2753425148054627013L;

	private final String newLang;
	
	private String oldLang;
	
	public SessionLanguageEdit(SessionEditor editor, String newLang) {
		super(editor);
		this.newLang = newLang;
	}

	@Override
	public void undo() {
		getEditor().getSession().setLanguage(oldLang);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LANG_CHANGED, getEditor().getUndoSupport(), oldLang);
		getEditor().getEventManager().queueEvent(ee);
	}
	
	@Override
	public void doIt() {
		oldLang = getEditor().getSession().getLanguage();
		getEditor().getSession().setLanguage(newLang);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LANG_CHANGED, getSource(), newLang);
		getEditor().getEventManager().queueEvent(ee);
	}

}
