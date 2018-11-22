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