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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllable.SyllableConstituentType;

public class ScTypeEdit extends SessionEditorUndoableEdit {

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
		
			final EditorEvent<SyllabificationAlignmentEditorView.ScEditData> ee =
					new EditorEvent<>(SyllabificationAlignmentEditorView.ScEdit, getEditor(),
							new SyllabificationAlignmentEditorView.ScEditData(transcript, index, scType, prevScType));
			getEditor().getEventManager().queueEvent(ee);
		}
	}
	
	@Override
	public void doIt() {
		if(index >= 0 && index < transcript.length()) {
			prevScType = transcript.elementAt(index).getScType();
			transcript.elementAt(index).setScType(scType);

			final EditorEvent<SyllabificationAlignmentEditorView.ScEditData> ee =
					new EditorEvent<>(SyllabificationAlignmentEditorView.ScEdit, getEditor(),
							new SyllabificationAlignmentEditorView.ScEditData(transcript, index, prevScType, scType));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}
