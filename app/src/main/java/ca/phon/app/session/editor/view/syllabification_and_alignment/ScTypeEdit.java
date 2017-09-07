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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllableConstituentType;

public class ScTypeEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -4921206917410378793L;

	private final IPATranscript transcript;
	
	private final int index;
	
	private final SyllableConstituentType scType;
	
	private SyllableConstituentType prevScType;
	
	public ScTypeEdit(SessionEditor editor, IPATranscript transcript, int index, SyllableConstituentType scType) {
		super(editor);
		this.transcript = transcript;
		this.index = index;
		this.scType = scType;
	}
	
	@Override
	public void undo() {
		if(prevScType != null && index >= 0 && index < transcript.length()) {
			transcript.elementAt(index).setScType(prevScType);
		
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getEditor().getUndoSupport(), transcript);
			getEditor().getEventManager().queueEvent(ee);
		}
	}
	
	@Override
	public void doIt() {
		if(index >= 0 && index < transcript.length()) {
			prevScType = transcript.elementAt(index).getScType();
			transcript.elementAt(index).setScType(scType);
		
			final EditorEvent ee = new EditorEvent(SyllabificationAlignmentEditorView.SC_EDIT, getSource(), transcript);
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}
