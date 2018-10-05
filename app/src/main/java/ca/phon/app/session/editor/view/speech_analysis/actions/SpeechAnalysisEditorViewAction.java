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
package ca.phon.app.session.editor.view.speech_analysis.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;

public abstract class SpeechAnalysisEditorViewAction extends SessionEditorAction {

	private static final long serialVersionUID = 3163798645290753856L;

	private final SpeechAnalysisEditorView view;

	public SpeechAnalysisEditorViewAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor);
		this.view = view;
	}

	public SpeechAnalysisEditorView getView() {
		return this.view;
	}
	
}
