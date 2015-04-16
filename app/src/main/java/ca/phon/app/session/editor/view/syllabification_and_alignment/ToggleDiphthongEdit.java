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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllabificationInfo;

public class ToggleDiphthongEdit extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = -4921206917410378793L;

	private final IPATranscript transcript;
	
	private final int index;
	
	public ToggleDiphthongEdit(SessionEditor editor, IPATranscript transcript, int index) {
		super(editor);
		this.transcript = transcript;
		this.index = index;
	}
	
	@Override
	public void undo() {
		super.redo();
	}

	@Override
	public void doIt() {
		if(index >= 0 && index < transcript.length()) {
			final IPAElement ele = transcript.elementAt(index);
			final SyllabificationInfo info = ele.getExtension(SyllabificationInfo.class);
			info.setDiphthongMember(!info.isDiphthongMember());
			
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getSource(), transcript);
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}
